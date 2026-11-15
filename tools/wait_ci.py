#!/usr/bin/env python3
"""Watch one exact CI run with one metadata request per interval, never fetch every Check.

A completed run is successful only when GitHub explicitly reports success.
This avoids gh-version-dependent watch exit codes and repeated large job/Check lists.
"""
import argparse
import json
import math
import re
import subprocess
import time

REPOSITORY = 'Santi-PdR/Special-Pickaxes'


def validate_run(run, run_id, commit):
    if not isinstance(run, dict) or type(run.get('id')) is not int or run['id'] != run_id:
        raise ValueError('CI response does not match the requested run')
    if run.get('head_sha') != commit:
        raise ValueError('CI response does not match the requested source commit')
    status = run.get('status')
    if status not in ('queued', 'in_progress', 'completed', 'waiting', 'pending', 'requested'):
        raise ValueError('Unknown CI run status: ' + str(status))
    if status == 'completed':
        if not isinstance(run.get('conclusion'), str) or not run['conclusion']:
            raise ValueError('Completed CI run has no conclusion')
        return 0 if run['conclusion'] == 'success' else 1
    return None


def read_run(run_id):
    result = subprocess.run(['gh', 'api', f'repos/{REPOSITORY}/actions/runs/{run_id}'],
                            capture_output=True, text=True)
    if result.returncode:
        # Do not retry quota/authentication errors in a tight loop or mislabel them as test failures.
        raise RuntimeError('Unable to read CI status: ' + result.stderr[-2000:])
    return json.loads(result.stdout)


def wait_for_run(run_id, commit, interval=60, timeout=3600):
    if type(run_id) is not int or run_id <= 0 or not re.fullmatch(r'[0-9a-f]{40}', commit):
        raise ValueError('An actual positive run ID and full source SHA are required')
    if not math.isfinite(interval) or not math.isfinite(timeout) or interval < 15 or timeout <= 0:
        raise ValueError('Interval must be at least 15 seconds and timeout must be positive')
    deadline = time.monotonic() + timeout
    while True:
        run = read_run(run_id)
        result = validate_run(run, run_id, commit)
        print(f'CI {run_id}: {run["status"]}; conclusion={run.get("conclusion") or "pending"}', flush=True)
        if result is not None:
            return result
        remaining = deadline - time.monotonic()
        if remaining <= 0:
            print('CI_WAIT_TIMEOUT: the run is not yet verified', flush=True)
            return 124
        time.sleep(min(interval, remaining))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('run_id', type=int)
    parser.add_argument('commit')
    parser.add_argument('--interval', type=float, default=60)
    parser.add_argument('--timeout', type=float, default=3600)
    args = parser.parse_args()
    try:
        return wait_for_run(args.run_id, args.commit, args.interval, args.timeout)
    except (RuntimeError, ValueError, OSError) as error:
        print(str(error), flush=True)
        return 2


if __name__ == '__main__':
    raise SystemExit(main())
