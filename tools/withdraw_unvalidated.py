import json,subprocess
for sha in ['2c9db17','c85eff8','7e8a1ea']:
 checks=json.loads(subprocess.check_output(['gh','api',f'repos/Santi-PdR/Special-Pickaxes/commits/{sha}/check-runs?per_page=100']))['check_runs']
 for c in checks:
  if c['name'].startswith('Build evidence '):
   payload={'output':{'title':'Unvalidated export withdrawn','summary':'No release approved. See logs-only diagnostics in the Actions run.','text':'The legacy binary-bearing export has been withdrawn.'}}
   subprocess.run(['gh','api','--method','PATCH',f'repos/Santi-PdR/Special-Pickaxes/check-runs/{c["id"]}','--input','-'],input=json.dumps(payload),text=True,stdout=subprocess.DEVNULL,check=True)
