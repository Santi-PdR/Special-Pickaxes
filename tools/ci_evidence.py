#!/usr/bin/env python3
"""Publish reproducible evidence through Checks API, also accessible when blob artifact CDN is blocked.
Only explicit build products are exported. No environment, credentials or world data.
"""
import base64,hashlib,json,os,pathlib,subprocess,time
from evidence_bundle import make_bundle
# Keep one genuine in-game held screenshot per tool in the bounded review bundle.
# Resize only; never synthesize a hand view from the inventory sprite.
from mining_catalog import IDS
capture_dir=pathlib.Path('build/client-smoke')
for pick in IDS:
 candidates=sorted(capture_dir.glob('ux-'+pick+'-*.jpg'))
 if candidates:
  subprocess.run(['java','tools/ResizePortrait.java',str(candidates[0]),str(capture_dir/('portrait-'+pick+'.jpg'))],check=True)
root=pathlib.Path('.'); files=[]
for pattern in ['build/client-smoke/*.jpg','build/client-smoke/image-manifest.json','build/client-smoke/client.log','build/RELEASE.json','build/SHA256SUMS','build/test-results/test/*.xml','run/logs/gametest.log','run/logs/latest.log','run/*test*.xml','build/packaged-smoke/console.log','build/verification-*.log']:
 files.extend(root.glob(pattern))
if os.environ.get('PUBLISH_BINARY')=='true':files.extend(root.glob('build/libs/*.jar'))
files=sorted(set(p for p in files if p.is_file()))
subprocess.run(['python3','tools/publish_diagnostics.py','build/verification-build.log','build/verification-gametest.log','build/verification-client.log'],check=True)
# Keep all diagnostics, bound the Checks subset, and identify omitted images explicitly.
# Full-resolution captures are still uploaded separately in forge-validation.
data,manifest,report=make_bundle({str(p):p.read_bytes() for p in files})
(root/'build/evidence-manifest.json').write_text(json.dumps(manifest,indent=2))
(root/'build/evidence-export.json').write_text(json.dumps(report,indent=2)+'\n')
encoded=base64.b64encode(data).decode();chunks=[encoded[i:i+48000] for i in range(0,len(encoded),48000)]
print(f'Exporting {len(manifest)} files, {len(data)} compressed bytes in {len(chunks)} bounded chunks; omitted images={len(report["omitted_images"])}')
for i,chunk in enumerate(chunks):
 payload={'name':f'Build evidence {i+1:02d}/{len(chunks):02d}', 'head_sha':os.environ['GITHUB_SHA'],
  'status':'completed','conclusion':'neutral','output':{'title':'Downloadable build and verification evidence',
   'summary':f'Run {os.environ["GITHUB_RUN_ID"]}. ZIP base64 chunk {i+1}/{len(chunks)}. SHA256(zip): {hashlib.sha256(data).hexdigest()}',
   'text':'```base64\n'+chunk+'\n```'}}
 # Content-creation endpoints have separate secondary limits from the core API quota.
 for attempt in range(4):
  result=subprocess.run(['gh','api',f'repos/{os.environ["GITHUB_REPOSITORY"]}/check-runs','--input','-'],
      input=json.dumps(payload),text=True,capture_output=True)
  if result.returncode==0:break
  if attempt==3:raise RuntimeError('Evidence chunk publication failed: '+result.stderr[-1000:])
  time.sleep((5,15,45)[attempt])
 print(json.loads(result.stdout)['html_url'])
 time.sleep(1.5)
