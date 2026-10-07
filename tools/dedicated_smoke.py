#!/usr/bin/env python3
"""Boot the reobfuscated release JAR in a clean Forge dedicated server, then stop cleanly."""
import pathlib, subprocess, shutil, time, threading, queue, urllib.request, sys
root=pathlib.Path.cwd(); server=root/'build/packaged-smoke'; server.mkdir(parents=True,exist_ok=True)
installer=server/'forge-installer.jar'
if not installer.exists():
 subprocess.run(['curl','--fail','--location','--retry','3','https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.3.0/forge-1.20.1-47.3.0-installer.jar','--output',str(installer)],check=True)
subprocess.run(['java','-jar',str(installer),'--installServer'],cwd=server,check=True)
(server/'eula.txt').write_text('eula=true\n')
(server/'server.properties').write_text('online-mode=false\nserver-port=25575\nlevel-name=smoke-world\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\nmax-tick-time=60000\n')
(server/'mods').mkdir(exist_ok=True)
jar=root/'build/libs/special-pickaxes-1.20.1-4.0.0.jar';shutil.copy2(jar,server/'mods'/jar.name)
proc=subprocess.Popen(['java','-Xmx2G','@libraries/net/minecraftforge/forge/1.20.1-47.3.0/unix_args.txt','nogui'],cwd=server,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,bufsize=1)
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
   # Parse all ten item IDs on the real installed server using console commands.
   for id in 'palimpsest fault_choir eventide meridian paradox_crucible interregnum worldloom icarus hollow_axiom bifold_atlas'.split():
    proc.stdin.write(f'give @a specialpickaxes:{id}\n')
   proc.stdin.write('save-all\nstop\n');proc.stdin.flush();stopped=True
 if proc.poll() is None:proc.wait(timeout=30)
finally:
 if proc.poll() is None:proc.kill();proc.wait()
 (server/'console.log').write_text(''.join(output))
if not started or not stopped or proc.returncode!=0:sys.exit('Packaged server did not complete a clean start/stop')
print('PACKAGED_SERVER_SMOKE_OK')
