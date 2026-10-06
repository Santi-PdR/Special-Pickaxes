#!/usr/bin/env python3
"""Verify the built release, then publish its SHA/size/commit manifest for artifact consumers."""
import hashlib,json,os,pathlib,struct,subprocess,tomllib,zipfile
root=pathlib.Path('.');jar=root/'build/libs/special-pickaxes-1.20.1-3.1.0.jar'
ids='palimpsest fault_choir eventide meridian paradox_crucible interregnum worldloom icarus hollow_axiom bifold_atlas worldbreaker chronicle keystone tessellator aegis lodestar seam_ripper causeway counterseal covenant'.split()
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
 assert len([n for n in z.namelist() if '/models/item/' in n and n.endswith('.json')])==20
 assert not any('/textures/item/' in n for n in z.namelist())
 assert 'io/github/santipdr/specialpickaxes/artifact/ArtifactActions.class' in z.namelist()
 assert 'io/github/santipdr/specialpickaxes/artifact/WorkQueue.class' in z.namelist()
digest=hashlib.sha256(jar.read_bytes()).hexdigest()
info={'file':jar.name,'bytes':jar.stat().st_size,'sha256':digest,'commit':sha,'run_id':os.environ.get('GITHUB_RUN_ID'),'artifacts':ids}
(root/'build/RELEASE.json').write_text(json.dumps(info,indent=2)+'\n')
(root/'build/SHA256SUMS').write_text(digest+'  '+jar.name+'\n')
print(json.dumps(info,indent=2))
