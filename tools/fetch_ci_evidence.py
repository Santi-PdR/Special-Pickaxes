#!/usr/bin/env python3
"""Fetch bounded CI evidence through GitHub Checks (fallback for blocked artifact CDNs)."""
import argparse,base64,hashlib,json,pathlib,subprocess
from evidence_archive import extract_verified
from evidence_checks import select_export
p=argparse.ArgumentParser();p.add_argument('sha');p.add_argument('run_id');p.add_argument('destination');args=p.parse_args()
repo=subprocess.check_output(['gh','repo','view','--json','nameWithOwner','--jq','.nameWithOwner'],text=True).strip()
raw=subprocess.check_output(['gh','api','--paginate',f'repos/{repo}/commits/{args.sha}/check-runs?per_page=100','--jq','.check_runs[] | @json'],text=True)
try:
 checks,digest=select_export([json.loads(line) for line in raw.splitlines() if line.strip()],args.run_id)
except ValueError as error:raise SystemExit(str(error)) from error
data=base64.b64decode(''.join(c['output']['text'].split('```base64\n',1)[1].split('\n```',1)[0] for c in checks),validate=True)
if hashlib.sha256(data).hexdigest()!=digest:raise SystemExit('archive checksum mismatch')
root=pathlib.Path(args.destination).resolve()
for entry in extract_verified(data,root):
 print(entry['sha256'],entry['bytes'],entry['path'])
