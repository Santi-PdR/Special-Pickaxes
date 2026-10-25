"""Best-effort withdrawal of superseded evidence via the GitHub Checks API."""
import json,subprocess

failed=False
for sha in ['2c9db17','c85eff8','7e8a1ea']:
 result=subprocess.run(['gh','api',f'repos/Santi-PdR/Special-Pickaxes/commits/{sha}/check-runs?per_page=100'],capture_output=True,text=True)
 if result.returncode:
  detail=(result.stderr or result.stdout or 'GitHub API request failed').strip()
  print('WARNING: Could not inspect superseded evidence checks; continuing validation. '+detail[-1200:])
  break
 try:checks=json.loads(result.stdout).get('check_runs',[])
 except (json.JSONDecodeError,AttributeError):
  print('WARNING: GitHub returned an unreadable checks response; continuing validation.')
  break
 for check in checks:
  if not check.get('name','').startswith('Build evidence '):continue
  payload={'output':{'title':'Unvalidated export withdrawn','summary':'No release approved. See logs-only diagnostics in the Actions run.','text':'The legacy binary-bearing export has been withdrawn.'}}
  result=subprocess.run(['gh','api','--method','PATCH',f'repos/Santi-PdR/Special-Pickaxes/check-runs/{check["id"]}','--input','-'],input=json.dumps(payload),text=True,capture_output=True)
  if result.returncode:
   detail=(result.stderr or result.stdout or 'GitHub API request failed').strip()
   print('WARNING: Could not withdraw a superseded evidence check; continuing validation. '+detail[-1200:])
   failed=True
   break
 if failed:break
