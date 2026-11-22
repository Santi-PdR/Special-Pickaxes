#!/usr/bin/env python3
"""Publish bounded build diagnostics when Actions log/blob downloads are unavailable."""
import json,os,pathlib,subprocess,sys
parts=[]
for name in sys.argv[1:]:
 p=pathlib.Path(name)
 if p.is_file():
  text=p.read_text(errors='replace')
  lines=text.splitlines();index=next((i for i,line in enumerate(lines) if any(term in line for term in ['IllegalStateException:', 'AssertionError:', 'error:', 'Exception in server tick loop'])),None)
  first='\n'.join(lines[max(0,index-3):index+25])[:5000] if index is not None else ''
  parts.append('## '+p.name+'\nFirst error excerpt (full log retained in Actions artifact):\n```text\n'+first+'\n```\nTail:\n```text\n'+text[-11000:]+'\n```')
payload={'name':'Forge diagnostics','head_sha':os.environ['GITHUB_SHA'],'status':'completed','conclusion':'neutral','output':{'title':'Bounded runtime diagnostic tails','summary':'Run '+os.environ['GITHUB_RUN_ID'],'text':'\n'.join(parts)[-55000:] or 'No logs yet.'}}
subprocess.run(['gh','api',f'repos/{os.environ["GITHUB_REPOSITORY"]}/check-runs','--input','-'],input=json.dumps(payload),text=True,stdout=subprocess.DEVNULL,check=True)
