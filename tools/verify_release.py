#!/usr/bin/env python3
"""Verify the built release, then publish its SHA/size/commit manifest for artifact consumers."""
import hashlib,json,os,pathlib,re,struct,subprocess,tomllib,zipfile
root=pathlib.Path('.')
version=re.search(r"^version\s*=\s*'([^']+)'",(root/'build.gradle').read_text(),re.M).group(1)
jar=root/f'build/libs/special-pickaxes-1.20.1-{version}.jar'
from mining_catalog import IDS as ids
sha=subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip()
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 assert tomllib.loads(z.read('META-INF/mods.toml').decode())['mods'][0]['modId']=='specialpickaxes'
 manifest=z.read('META-INF/MANIFEST.MF').decode().replace('\r\n ','').replace('\r\n','\n')
 assert 'Build-Commit: '+sha in manifest,manifest
 assert not any('/recipes/' in n or '/worldgen/' in n for n in z.namelist())
 assert not any('/test/' in n for n in z.namelist())
 assert all(struct.unpack('>H',z.read(n)[6:8])[0]==61 for n in z.namelist() if n.endswith('.class'))
 for id in ids:
  for n in [f'assets/specialpickaxes/models/item/{id}.json']:assert n in z.namelist()
 assert len([n for n in z.namelist() if '/models/item/' in n and n.endswith('.json')])==len(ids)
 assert len([n for n in z.namelist() if '/textures/item/' in n and n.endswith('.png')])==len(ids)
 effect_ids=re.findall(r'EFFECTS\.register\("([^"]+)"',(root/'src/main/java/io/github/santipdr/specialpickaxes/SpecialPickaxes.java').read_text())
 assert effect_ids,'No registered mob effects found'
 for effect_id in effect_ids:
  icon=f'assets/specialpickaxes/textures/mob_effect/{effect_id}.png'
  assert icon in z.namelist(),f'Missing mob effect HUD icon: {icon}'
  image=z.read(icon)
  assert image[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',image[16:24])==(16,16),f'Invalid mob effect HUD icon: {icon}'
 assert 'io/github/santipdr/specialpickaxes/artifact/ArtifactActions.class' in z.namelist()
 assert 'io/github/santipdr/specialpickaxes/artifact/WorkQueue.class' in z.namelist()
 assert 'io/github/santipdr/specialpickaxes/artifact/CuriosCompat.class' in z.namelist()
 curios_slot='data/specialpickaxes/curios/slots/pickaxe.json'
 curios_entities='data/specialpickaxes/curios/entities/entities.json'
 curios_tag='data/curios/tags/items/pickaxe.json'
 assert all(path in z.namelist() for path in [curios_slot,curios_entities,curios_tag])
 slot=json.loads(z.read(curios_slot))
 assert slot['size']==1 and slot['icon']=='specialpickaxes:slot/empty_pickaxe_slot'
 slot_icon='assets/specialpickaxes/textures/slot/empty_pickaxe_slot.png'
 assert slot_icon in z.namelist() and z.read(slot_icon)[:8]==b'\x89PNG\r\n\x1a\n'
 assert 'pickaxe' in json.loads(z.read(curios_entities))['slots']
 assert set(json.loads(z.read(curios_tag))['values'])=={'specialpickaxes:'+item for item in ids}
digest=hashlib.sha256(jar.read_bytes()).hexdigest()
info={'file':jar.name,'bytes':jar.stat().st_size,'sha256':digest,'commit':sha,'run_id':os.environ.get('GITHUB_RUN_ID'),'artifacts':ids}
(root/'build/RELEASE.json').write_text(json.dumps(info,indent=2)+'\n')
(root/'build/SHA256SUMS').write_text(digest+'  '+jar.name+'\n')
print(json.dumps(info,indent=2))
