"""Choose one complete, most-recent Checks export; never stitch separate CI attempts."""
import re


def select_export(checks, run_id):
    groups = {}
    for check in checks:
        summary = check.get('output', {}).get('summary') or ''
        name = re.fullmatch(r'Build evidence (\d+)/(\d+)', check.get('name', ''))
        digest = re.search(r'SHA256\(zip\): ([0-9a-f]{64})\s*$', summary)
        if not name or f'Run {run_id}.' not in summary or not digest:
            continue
        attempt = re.search(r'\bAttempt (\d+)\.', summary)
        attempt = int(attempt[1]) if attempt else 1
        key = (attempt, digest[1])
        groups.setdefault(key, []).append(check)
    if not groups:
        raise ValueError('No evidence available for this run')
    # Even an incomplete newer attempt must prevent fallback to a stale green export.
    key = max(groups, key=lambda key: (key[0], max(c['id'] for c in groups[key])))
    selected = groups[key]
    parts = {}
    expected = set()
    for check in selected:
        match = re.fullmatch(r'Build evidence (\d+)/(\d+)', check['name'])
        index, total = map(int, match.groups())
        expected.add(total)
        if index not in parts or parts[index]['id'] < check['id']:
            parts[index] = check
    if len(expected) != 1:
        raise ValueError('Inconsistent evidence chunk totals')
    total = expected.pop()
    if not 1 <= total <= 128 or set(parts) != set(range(1, total + 1)):
        raise ValueError('Incomplete evidence export for the most recent attempt')
    return [parts[index] for index in range(1, total + 1)], key[1]
