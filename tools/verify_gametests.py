"""Gradle may exit zero after a Forge launch failure: require actual complete execution."""
import pathlib
import re
import sys


def expected_test_count(directory):
    return sum(len(re.findall(r'@GameTest\(', path.read_text()))
               for path in pathlib.Path(directory).glob('*GameTests.java'))


def validate_log(text, expected):
    if expected <= 0:
        raise ValueError('No GameTests discovered; refusing an empty verification')
    counts = re.findall(r'=+\s*(\d+) GAME TESTS COMPLETE\s*=+', text, re.I)
    if not counts or any(int(count) != expected for count in counts):
        raise ValueError(f'Missing or inconsistent completed GameTest summary for {expected} tests; observed {counts}')
    if re.search(r'\b[1-9]\d* required tests? failed', text, re.I):
        raise ValueError('GameTests reported failures')
    return expected


def main():
    if len(sys.argv) != 2:
        raise SystemExit('Usage: verify_gametests.py <log>')
    expected = expected_test_count('src/main/java/io/github/santipdr/specialpickaxes/test')
    try:
        validate_log(pathlib.Path(sys.argv[1]).read_text(errors='replace'), expected)
    except (OSError, ValueError) as error:
        raise SystemExit(str(error)) from error
    print(f'GAMETESTS_OK: {expected}')


if __name__ == '__main__':
    main()
