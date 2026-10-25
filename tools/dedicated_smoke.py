#!/usr/bin/env python3
"""Boot the reobfuscated release JAR in a clean Forge dedicated server, then stop cleanly."""
import pathlib, subprocess, shutil, time, threading, queue, urllib.request, sys, json, re
root=pathlib.Path.cwd(); server=root/'build/packaged-smoke'; server.mkdir(parents=True,exist_ok=True)
build_config=(root/'build.gradle').read_text()
match=re.search(r"""minecraft\\s+['"]net\\.minecraftforge:forge:([^'"]+)['"]""",build_config)
if not match:sys.exit('Could not resolve the Forge version from build.gradle')
forge_version=match.group(1)
installer=server/'forge-installer.jar'
if not installer.exists():
 url=f'https://maven.minecraftforge.net/net/minecraftforge/forge/{forge_version}/forge-{forge_version}-installer.jar'
 subprocess.run(['curl','--fail','--location','--retry','3',url,'--output',str(installer)],check=True)
subprocess.run(['java','-jar',str(installer),'--installServer'],cwd=server,check=True)
args_file=server/'libraries/net/minecraftforge/forge'/forge_version/'unix_args.txt'
if not args_file.is_file():sys.exit(f'Forge server arguments not found for {forge_version}: {args_file}')
(server/'eula.txt').write_text('eula=true\n')
(server/'server.properties').write_text('online-mode=false\nserver-port=25575\nlevel-name=smoke-world\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\nmax-tick-time=60000\n')
(server/'mods').mkdir(exist_ok=True)
release=json.loads((root/'build/RELEASE.json').read_text())
jar=root/'build/libs'/release['file']
if not jar.is_file():sys.exit(f'Built release JAR not found: {jar}')
shutil.copy2(jar,server/'mods'/jar.name)
proc=subprocess.Popen(['java','-Xmx2G',f'@{args_file}','nogui'],cwd=server,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,bufsize=1)
lines=queue.Queue()
def collect():
 for line in proc.stdout:lines.put(line)
threading.Thread(target=collect,daemon=True).start()
started=False;stopped=False;deadline=time.monotonic()+180; output=[]
try:
 while time.monotonic()<deadline:
  try: line=lines.get(timeout=1)
  except queue.Empty:
   if proc.poll() is not None:break
   continue
  output.append(line); print(line,end='')
  if 'Done (' in line and not started:
   started=True
   # Parse all nine item IDs on the real installed server using console commands.
   for id in __import__('mining_catalog').IDS:
    proc.stdin.write(f'give @a specialpickaxes:{id}\n')
   proc.stdin.write('save-all\nstop\n');proc.stdin.flush();stopped=True
 if proc.poll() is None:proc.wait(timeout=30)
finally:
 if proc.poll() is None:proc.kill();proc.wait()
 (server/'console.log').write_text(''.join(output))
if not started or not stopped or proc.returncode!=0:sys.exit('Packaged server did not complete a clean start/stop')
print('PACKAGED_SERVER_SMOKE_OK')
