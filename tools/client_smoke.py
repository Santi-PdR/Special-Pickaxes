#!/usr/bin/env python3
"""Real Forge client + integrated-server gallery under Xvfb. Uses only a disposable copy of the smoke world."""
import json,os,pathlib,queue,shutil,signal,subprocess,threading,time
root=pathlib.Path.cwd();out=root/'build/client-smoke';out.mkdir(parents=True,exist_ok=True)
world=root/'run/saves/ArtifactSmoke'
if world.exists():shutil.rmtree(world)
shutil.copytree(root/'build/packaged-smoke/smoke-world',world)
pack=world/'datapacks/artifact-gallery';functions=pack/'data/artifact_gallery/functions';functions.mkdir(parents=True,exist_ok=True)
(pack/'pack.mcmeta').write_text(json.dumps({'pack':{'pack_format':15,'description':'Disposable CI gallery (not mod worldgen)'}}))
for name in ['load','tick']:
 p=pack/f'data/minecraft/tags/functions/{name}.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps({'values':['artifact_gallery:'+name]}))
(functions/'load.mcfunction').write_text('scoreboard objectives add artifactAge dummy\n')
(functions/'tick.mcfunction').write_text('''scoreboard players add @a artifactAge 1
execute as @a[scores={artifactAge=1}] run gamemode creative @s
execute as @a[scores={artifactAge=1}] run tp @s 0 65 12 180 -6
execute as @a[tag=!artifact_gallery,scores={artifactAge=60..}] run function artifact_gallery:setup
''')
ids='palimpsest fault_choir eventide meridian paradox_crucible interregnum worldloom icarus hollow_axiom bifold_atlas'.split()
commands=['tag @s add artifact_gallery','fill -9 65 0 9 74 16 minecraft:air','fill -9 64 -1 9 64 17 minecraft:polished_andesite','fill -9 65 0 9 74 0 minecraft:black_concrete','fill -9 74 0 9 74 16 minecraft:sea_lantern','time set noon','weather clear','gamerule doDaylightCycle false','gamerule doMobSpawning false','tp @s 0 65 12 180 -6','clear @s']
for i,id in enumerate(ids):
 x=-6+(i%5)*3;y=69 if i<5 else 66
 commands.append(f'give @s specialpickaxes:{id}')
 commands.append(f'''summon minecraft:item_display {x} {y} 3 {{item:{{id:"specialpickaxes:{id}",Count:1b}},item_display:"gui",billboard:"center",transformation:{{scale:[2.0f,2.0f,2.0f],translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}}}''')
 commands.append(f'''summon minecraft:text_display {x} {y-1} 3 {{text:'{{"text":"{id}","color":"white"}}',billboard:"center",alignment:"center",background:0,line_width:200}}''')
commands+=['tellraw @s {"text":"ARTIFACT_CLIENT_SMOKE_READY"}']
(functions/'setup.mcfunction').write_text('\n'.join(commands)+'\n')
(root/'run/options.txt').write_text('tutorialStep:none\npauseOnLostFocus:false\nrenderDistance:4\nsimulationDistance:5\nmaxFps:30\nsoundCategory_music:0.0\nlang:es_es\n')
(out/'alsoft.conf').write_text('[general]\nrt-prio=0\ndrivers=null\n')
env=dict(os.environ,DISPLAY=':99',LIBGL_ALWAYS_SOFTWARE='1',ALSOFT_DRIVERS='null',ALSOFT_CONF=str(out/'alsoft.conf'),NO_AT_BRIDGE='1')
xlog=open(out/'xvfb.log','w');xvfb=subprocess.Popen(['Xvfb',':99','-screen','0','1280x720x24','-ac'],stdout=xlog,stderr=subprocess.STDOUT)
proc=None;lines=queue.Queue();captured=[];ready=None;success=False
try:
 for _ in range(100):
  if pathlib.Path('/tmp/.X11-unix/X99').exists():break
  if xvfb.poll() is not None:raise RuntimeError('Xvfb failed to start')
  time.sleep(0.1)
 proc=subprocess.Popen(['./gradlew','runClient','-PclientSmoke','--stacktrace'],env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,bufsize=1,start_new_session=True)
 def read():
  for line in proc.stdout:lines.put(line)
 threading.Thread(target=read,daemon=True).start()
 deadline=time.monotonic()+420
 while time.monotonic()<deadline:
  try:line=lines.get(timeout=1)
  except queue.Empty:
   if proc.poll() is not None:break
   line=''
  if line:
   captured.append(line);print(line,end='')
   if '[CHAT]' in line and 'ARTIFACT_CLIENT_SMOKE_READY' in line:ready=time.monotonic()
  if ready and time.monotonic()-ready>8:
   subprocess.run(['java','-Djava.awt.headless=false',str(root/'tools/CaptureScreen.java'),str(out/'gallery.png')],env=env,check=True)
   subprocess.run(['java','-Djava.awt.headless=false',str(root/'tools/CaptureScreen.java'),str(out/'gallery-clean.png'),'clean'],env=env,check=True)
   success=True;break
finally:
 # Save the runtime log BEFORE the intentional termination of the disposable graphical client.
 latest=root/'run/logs/latest.log'
 if latest.exists():shutil.copy2(latest,out/'client.log')
 (out/'console.log').write_text(''.join(captured))
 if proc and proc.poll() is None:
  os.killpg(proc.pid,signal.SIGTERM)
  try:proc.wait(timeout=20)
  except subprocess.TimeoutExpired:os.killpg(proc.pid,signal.SIGKILL);proc.wait()
 xvfb.terminate();xvfb.wait(timeout=10);xlog.close()
if not success:raise SystemExit('Graphical client did not enter the artifact gallery before its deadline')
subprocess.run(['python3','tools/validate_runtime_logs.py',str(out/'client.log')],check=True)
text=(out/'client.log').read_text().lower()
for issue in ['missing texture','unable to load model','unable to bake','using missing texture']:
 if issue in text:raise SystemExit('Client resource error: '+issue)
print('GRAPHICAL_CLIENT_GALLERY_OK')
