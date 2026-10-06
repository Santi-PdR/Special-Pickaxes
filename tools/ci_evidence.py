#!/usr/bin/env python3
"""Publish reproducible evidence through Checks API, also accessible when blob artifact CDN is blocked.
Only explicit build products are exported. No environment, credentials or world data.
"""
import base64,hashlib,io,json,os,pathlib,subprocess,zipfile
root=pathlib.Path('.'); files=[]
for pattern in ['build/libs/*.jar','build/test-results/test/*.xml','run/logs/latest.log','run/*test*.xml','build/packaged-smoke/console.log','build/verification-*.log']:
 files.extend(root.glob(pattern))
files=sorted(set(p for p in files if p.is_file()))
manifest=[{'path':str(p),'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in files]
(root/'build/evidence-manifest.json').write_text(json.dumps(manifest,indent=2))
b=io.BytesIO()
with zipfile.ZipFile(b,'w',zipfile.ZIP_DEFLATED) as z:
 for p in files:z.write(p,str(p))
 z.writestr('manifest.json',json.dumps(manifest,indent=2))
encoded=base64.b64encode(b.getvalue()).decode();chunks=[encoded[i:i+48000] for i in range(0,len(encoded),48000)]
if len(chunks)>16:raise RuntimeError('Evidence exceeds bounded Checks export budget')
for i,chunk in enumerate(chunks):
 payload={'name':f'Build evidence {i+1:02d}/{len(chunks):02d}', 'head_sha':os.environ['GITHUB_SHA'],
  'status':'completed','conclusion':'neutral','output':{'title':'Downloadable build and verification evidence',
   'summary':f'Run {os.environ["GITHUB_RUN_ID"]}. ZIP base64 chunk {i+1}/{len(chunks)}. SHA256(zip): {hashlib.sha256(b.getvalue()).hexdigest()}',
   'text':'```base64\n'+chunk+'\n```'}}
 result=subprocess.run(['gh','api',f'repos/{os.environ["GITHUB_REPOSITORY"]}/check-runs','--input','-'],
     input=json.dumps(payload),text=True,capture_output=True,check=True)
 print(json.loads(result.stdout)['html_url'])
