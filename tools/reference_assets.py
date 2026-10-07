"""Curated adaptation of the four user-authorized reference archives. Never modifies a JAR."""
import pathlib,zipfile,json,hashlib,struct
SPECS=[
('palimpsest','CustomPickaxes','vitalite_pickaxe'),('fault_choir','CustomPickaxes','echo_pickaxe'),
('eventide','PicksOfPower','schizo_pickaxe'),('meridian','PicksOfPower','ender_pickaxe'),
('paradox_crucible','CustomPickaxes','volcanite_pickaxe'),('interregnum','ElementalUtilityPickaxes','lighningpickaxe'),
('worldloom','UltimatePickaxes','natural_pickaxe'),('icarus','PicksOfPower','elytra_pickaxe'),
('hollow_axiom','UltimatePickaxes','shadow_pickaxe'),('bifold_atlas','ElementalUtilityPickaxes','adamantiumpickaxe'),
('worldbreaker','PicksOfPower','inferno_reaver'),('chronicle','CustomPickaxes','stormstone_pickaxe'),
('keystone','ElementalUtilityPickaxes','hammerpickaxe'),('tessellator','CustomPickaxes','titanium_pickaxe'),
('aegis','ElementalUtilityPickaxes','pickaxe3'),('lodestar','PicksOfPower','seismic_pickaxe'),
('seam_ripper','ElementalUtilityPickaxes','pickaxe1'),('causeway','ElementalUtilityPickaxes','longpickaxe'),
('counterseal','ElementalUtilityPickaxes','sculkpickaxe'),('covenant','UltimatePickaxes','bone_pickaxe')]
def install(root):
 manifest=[]
 for id,folder,base in SPECS:
  jar=next(pathlib.Path('referencias',folder).glob('*.jar'))
  with zipfile.ZipFile(jar) as z:
   name=next(n for n in z.namelist() if n.endswith('/textures/item/'+base+'.png'))
   raw=z.read(name);dest=root/f'assets/specialpickaxes/textures/item/{id}.png';dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(raw)
   if name+'.mcmeta' in z.namelist():dest.with_suffix('.png.mcmeta').write_bytes(z.read(name+'.mcmeta'))
   else:
    w,h=struct.unpack('>II',raw[16:24])
    if h>w:dest.with_suffix('.png.mcmeta').write_text(json.dumps({'animation':{'frametime':4}}))
   model=root/f'assets/specialpickaxes/models/item/{id}.json';model.parent.mkdir(parents=True,exist_ok=True);model.write_text(json.dumps({'parent':'minecraft:item/handheld','textures':{'layer0':'specialpickaxes:item/'+id}},indent=2)+'\n')
   manifest.append({'artifact':id,'archive':str(jar),'entry':name,'sha256':hashlib.sha256(raw).hexdigest(),'adaptation':'Independent item model, effect palette and behavior; reference sprite retained without destructive repainting.'})
 pathlib.Path('docs/4.0').mkdir(parents=True,exist_ok=True)
 pathlib.Path('docs/4.0/ASSET-PROVENANCE.json').write_text(json.dumps(manifest,indent=2)+'\n')
