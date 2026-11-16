"""Retry only transient dependency-resolution failures before any Gradle task starts.
Never retries compiler/test failures, missing artifacts, TLS failures or arbitrary errors.
All attempts stay in stdout so CI retains evidence; final process status is preserved.
"""
import subprocess
import sys
import time


def transient_resolution_failure(text):
    # No work may have run: a task failure must never be classified by incidental
    # network strings printed elsewhere in its output.
    if '> Task ' in text or 'BUILD SUCCESSFUL' in text:
        return False
    if 'A problem occurred configuring' not in text or 'Could not resolve' not in text:
        return False
    if any(marker in text for marker in ('Could not find ', 'PKIX',
                                         'SSLHandshakeException', '401 Unauthorized', '403 Forbidden')):
        return False
    return any(marker in text for marker in ('Read timed out', 'connect timed out',
                                             '429 from server', '429 Too Many Requests'))


MAX_DIAGNOSTIC_CHARS = 256_000


def execute(command, **unused):
    """Stream the complete log to CI, retain only a bounded tail for classification.

    A task marker seen before tail eviction must still forbid a retry. Truncated
    diagnostics also fail closed: incomplete evidence is not enough to retry.
    """
    tail = ''
    truncated = False
    with subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                          text=True, errors='replace') as process:
        while True:
            chunk = process.stdout.read(4096)
            if not chunk:
                break
            print(chunk, end='', flush=True)
            tail += chunk
            if len(tail) > MAX_DIAGNOSTIC_CHARS:
                truncated = True
                tail = tail[-MAX_DIAGNOSTIC_CHARS:]
        code = process.wait()
    if truncated:
        # The classifier already refuses output with a task marker. Do not infer
        # that no tasks ran from a tail whose early evidence has been evicted.
        tail = '> Task :diagnostic_tail_truncated_no_retry\n' + tail
    result = subprocess.CompletedProcess(command, code, tail)
    result.streamed = True
    return result


def run(command, runner=execute, sleeper=time.sleep):
    if not command:
        raise ValueError('Missing Gradle command')
    for attempt in range(1, 3):
        print(f'DEPENDENCY_ATTEMPT {attempt}/2', flush=True)
        result = runner(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        if not getattr(result, 'streamed', False):
            print(result.stdout, end='' if result.stdout.endswith('\n') else '\n', flush=True)
        if result.returncode == 0:
            return 0
        if attempt == 2 or not transient_resolution_failure(result.stdout):
            return result.returncode if result.returncode > 0 else 1
        print('Transient configuration download failure before tasks; retrying once in 60 seconds.', flush=True)
        sleeper(60)
    return 1


if __name__ == '__main__':
    args = sys.argv[1:]
    if args[:1] == ['--']:
        args = args[1:]
    if not args:
        raise SystemExit('Usage: gradle_dependency_retry.py -- ./gradlew build test ...')
    raise SystemExit(run(args))
