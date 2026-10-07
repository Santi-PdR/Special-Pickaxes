#!/usr/bin/env python3
"""Fail on runtime errors, not just on the server process exit code."""
import pathlib,re,sys
paths=[pathlib.Path(p) for p in sys.argv[1:]]
if not paths:raise SystemExit('Usage: validate_runtime_logs.py <log>...')
issues=[]
pattern=re.compile(r'\b(?:ERROR|Exception|Crash|Stacktrace|Failed|Invalid)\b|Unknown item|Unknown command',re.I)
for path in paths:
 if not path.exists():issues.append(f'Missing log: {path}');continue
 for number,line in enumerate(path.read_text(errors='replace').splitlines(),1):
  if pattern.search(line):issues.append(f'{path}:{number}: {line}')
if issues:
 print('\n'.join(issues));raise SystemExit(1)
print('Runtime log scan clean:',', '.join(map(str,paths)))
