"""Withdraw legacy binary exports once, without rewriting identical Checks on every CI run."""
import json
from pathlib import Path
import subprocess
import time

LEGACY_COMMITS = ('2c9db17', 'c85eff8', '7e8a1ea')
REPOSITORY = 'Santi-PdR/Special-Pickaxes'
WITHDRAWN = {
    'title': 'Unvalidated export withdrawn',
    'summary': 'No release approved. See logs-only diagnostics in the Actions run.',
    'text': 'The legacy binary-bearing export has been withdrawn.',
}


def needs_withdrawal(check):
    if not check.get('name', '').startswith('Build evidence '):
        return False
    output = check.get('output') or {}
    return any(output.get(key) != value for key, value in WITHDRAWN.items())


def gh_json(endpoint, payload=None, paginate=False):
    command = ['gh', 'api', endpoint]
    if paginate:
        command += ['--paginate']
    if payload is not None:
        command += ['--method', 'PATCH', '--input', '-']
    for attempt in range(3):
        result = subprocess.run(command, input=json.dumps(payload) if payload is not None else None,
                                text=True, capture_output=True)
        if result.returncode == 0:
            if not paginate:
                return json.loads(result.stdout)
            # Older gh versions emit adjacent JSON documents and do not support --slurp.
            decoder = json.JSONDecoder()
            remaining = result.stdout.lstrip()
            pages = []
            while remaining:
                page, end = decoder.raw_decode(remaining)
                pages.append(page)
                remaining = remaining[end:].lstrip()
            if not pages:
                raise ValueError('Missing legacy Checks response')
            return pages
        transient = any(marker in result.stderr.lower() for marker in (
            'secondary rate limit', 'http 429', 'http 502', 'http 503', 'http 504',
            'tls handshake timeout', 'connection reset by peer'))
        if not transient or attempt == 2:
            raise RuntimeError('Legacy export withdrawal failed: ' + result.stderr[-2000:])
        time.sleep((5, 15)[attempt])


def main():
    inspected = changed = 0
    for sha in LEGACY_COMMITS:
        pages = gh_json(f'repos/{REPOSITORY}/commits/{sha}/check-runs?per_page=100', paginate=True)
        for page in pages:
            for check in page['check_runs']:
                inspected += 1
                if needs_withdrawal(check):
                    gh_json(f'repos/{REPOSITORY}/check-runs/{check["id"]}', {'output': WITHDRAWN})
                    changed += 1
    message = f'LEGACY_WITHDRAWAL_OK: inspected={inspected}; updated={changed}; unchanged exports were not rewritten\n'
    Path('build').mkdir(exist_ok=True)
    Path('build/verification-withdraw.log').write_text(message)
    print(message, end='')


if __name__ == '__main__':
    try:
        main()
    except Exception as error:
        Path('build').mkdir(exist_ok=True)
        Path('build/verification-withdraw.log').write_text(str(error) + '\n')
        raise
