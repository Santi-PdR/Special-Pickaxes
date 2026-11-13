#!/usr/bin/env python3
"""Fetch bounded CI evidence through GitHub Checks (fallback for blocked artifact CDNs)."""
import argparse,base64,hashlib,json,pathlib,subprocess
from evidence_archive import extract_verified
p=argparse.ArgumentParser();p.add_argument('sha');p.add_argument('run_id');p.add_argument('destination');args=p.parse_args()
repo=subprocess.check_output(['gh','repo','view','--json','nameWithOwner','--jq','.nameWithOwner'],text=True).strip()
raw=subprocess.check_output(['gh','api','--paginate',f'repos/{repo}/commits/{args.sha}/check-runs?per_page=100','--jq','.check_runs[] | @json'],text=True)
checks=[c for c in [json.loads(line) for line in raw.splitlines() if line.strip()] if c['name'].startswith('Build evidence ') and f'Run {args.run_id}.' in c['output']['summary']]
if not checks:raise SystemExit('No evidence available for this run')
checks.sort(key=lambda c:int(c['name'].split()[2].split('/')[0]))
expected=int(checks[0]['name'].split('/')[-1])
if len(checks)!=expected:raise SystemExit('Incomplete evidence export')
data=base64.b64decode(''.join(c['output']['text'].split('```base64\n',1)[1].split('\n```',1)[0] for c in checks),validate=True)
digest=checks[0]['output']['summary'].split('SHA256(zip): ')[1].strip()
if hashlib.sha256(data).hexdigest()!=digest:raise SystemExit('archive checksum mismatch')
root=pathlib.Path(args.destination).resolve()
for entry in extract_verified(data,root):
 print(entry['sha256'],entry['bytes'],entry['path'])
