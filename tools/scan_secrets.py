#!/usr/bin/env python3
"""Scan tracked text and optional release ZIP for common credential/private-key signatures."""
import pathlib,re,subprocess,sys,zipfile
patterns=[re.compile(rb'gh[pousr]_[A-Za-z0-9]{30,}'),re.compile(rb'github_pat_[A-Za-z0-9_]{40,}'),re.compile(rb'AKIA[0-9A-Z]{16}'),re.compile(rb'-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----')]
failures=[];count=0
def inspect(name,data):
 global count
 count+=1
 for pattern in patterns:
  if pattern.search(data):failures.append(name);break
for name in subprocess.check_output(['git','ls-files','-z']).decode().split('\0'):
 p=pathlib.Path(name)
 if name and p.is_file() and p.suffix not in ['.jar','.png','.nbt']:inspect(name,p.read_bytes())
for jar in sys.argv[1:]:
 with zipfile.ZipFile(jar) as z:
  for name in z.namelist():
   if not name.endswith('/') and not name.endswith('.png'):inspect(jar+':'+name,z.read(name))
if failures:raise SystemExit('Potential credential material in: '+', '.join(failures))
print(f'SECRET_SCAN_OK: {count} tracked/release entries checked; no matching signatures')
