#!/usr/bin/env python3
"""Fetch bounded CI evidence through GitHub Checks (fallback for blocked artifact CDNs)."""
import argparse,base64,hashlib,io,json,pathlib,re,subprocess,zipfile
p=argparse.ArgumentParser();p.add_argument('sha');p.add_argument('run_id');p.add_argument('destination');args=p.parse_args()
repo=subprocess.check_output(['gh','repo','view','--json','nameWithOwner','--jq','.nameWithOwner'],text=True).strip()
raw=subprocess.check_output(['gh','api','--paginate','--slurp',f'repos/{repo}/commits/{args.sha}/check-runs?per_page=100'],text=True)
checks=[c for c in [c for page in json.loads(raw) for c in page['check_runs']] if c['name'].startswith('Build evidence ') and f'Run {args.run_id}.' in c['output']['summary']]
if not checks:raise SystemExit('No evidence available for this run')
checks.sort(key=lambda c:int(c['name'].split()[2].split('/')[0]))
expected=int(checks[0]['name'].split('/')[-1])
if len(checks)!=expected:raise SystemExit('Incomplete evidence export')
data=base64.b64decode(''.join(c['output']['text'].split('```base64\n',1)[1].split('\n```',1)[0] for c in checks),validate=True)
digest=checks[0]['output']['summary'].split('SHA256(zip): ')[1].strip()
assert hashlib.sha256(data).hexdigest()==digest,'archive checksum mismatch'
root=pathlib.Path(args.destination).resolve();root.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(io.BytesIO(data)) as z:
 for name in z.namelist():
  assert (root/name).resolve().is_relative_to(root),'unsafe archive path'
 z.extractall(root)
for entry in json.loads((root/'manifest.json').read_text()):
 path=root/entry['path'];assert path.stat().st_size==entry['bytes']
 assert hashlib.sha256(path.read_bytes()).hexdigest()==entry['sha256']
 print(entry['sha256'],entry['bytes'],entry['path'])
