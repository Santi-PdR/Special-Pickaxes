#!/usr/bin/env python3
"""Fail on runtime errors, including qualified Java exception names, not only exit codes."""
import pathlib
import re
import sys

PATTERN = re.compile(
    r'\b(?:ERROR|Exception|Crash|Stacktrace|Failed|Invalid)\b|Unknown item|Unknown command'
    r'|\b(?:[A-Za-z_$][\w$]*\.)*[A-Za-z_$][\w$]*(?:Exception|Error)\b'
    r'|Traceback \(most recent call last\)', re.I)


def scan_logs(paths):
    issues = []
    for path in map(pathlib.Path, paths):
        if not path.is_file():
            issues.append(f'Missing log: {path}')
            continue
        for number, line in enumerate(path.read_text(errors='replace').splitlines(), 1):
            if PATTERN.search(line):
                issues.append(f'{path}:{number}: {line}')
    return issues


def main():
    if len(sys.argv) < 2:
        raise SystemExit('Usage: validate_runtime_logs.py <log>...')
    issues = scan_logs(sys.argv[1:])
    if issues:
        print('\n'.join(issues))
        raise SystemExit(1)
    print('Runtime log scan clean:', ', '.join(sys.argv[1:]))


if __name__ == '__main__':
    main()
