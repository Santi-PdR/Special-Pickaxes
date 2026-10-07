#!/usr/bin/env python3
import json,pathlib,struct,hashlib,unittest
ROOT=pathlib.Path('src/main/resources')
from mining_catalog import IDS, MODES

class Resources(unittest.TestCase):
 def test_json(self):
  for pattern in ['*.json','*.mcmeta']:
   for p in ROOT.rglob(pattern):json.loads(p.read_text())
 def test_reference_models(self):
  hashes=set()
  for id in IDS:
   model=json.loads((ROOT/f'assets/specialpickaxes/models/item/{id}.json').read_text())
   self.assertEqual(model['textures']['layer0'],'specialpickaxes:item/'+id)
   hashes.add(hashlib.sha256((ROOT/f'assets/specialpickaxes/textures/item/{id}.png').read_bytes()).hexdigest())
  self.assertEqual(len(hashes),9)
 def test_mining_modes_and_removed_models(self):
  self.assertEqual({p.stem for p in (ROOT/'assets/specialpickaxes/models/item').glob('*.json')},set(IDS))
  for lang in ['en_us','es_es']:
   d=json.loads((ROOT/f'assets/specialpickaxes/lang/{lang}.json').read_text())
   for artifact,modes in MODES.items():
    for mode in modes:
     self.assertIn(f'mining.identity.{artifact}.{mode}',d)
     self.assertIn(f'mining.how.{artifact}.{mode}',d)
 def test_current_controls(self):
  for lang in ['en_us','es_es']:
   d=json.loads((ROOT/f'assets/specialpickaxes/lang/{lang}.json').read_text())
   self.assertEqual(len([k for k in d if k.startswith('key.specialpickaxes.')]),7)
   for key in ['message.specialpickaxes.companion_active','status.specialpickaxes.paused','message.specialpickaxes.chunk_pause']:
    self.assertNotIn('agachado',d[key].lower());self.assertNotIn('sneak',d[key].lower());self.assertNotIn('usar',d[key].lower().replace('pausar',''))
 def test_no_energy_and_manual_keys(self):
  for lang in ['en_us','es_es']:
   data=json.loads((ROOT/f'assets/specialpickaxes/lang/{lang}.json').read_text())
   self.assertFalse(any('energy' in key for key in data))
   for id in IDS:
    for prefix in ['identity.','controls.','example.']:self.assertIn(prefix+'specialpickaxes.'+id,data)
 def test_languages(self):
  langs=[json.loads((ROOT/f'assets/specialpickaxes/lang/{lang}.json').read_text()) for lang in ['en_us','es_es']]
  self.assertEqual(set(langs[0]),set(langs[1]))
  for data in langs:
   for id in IDS:
    for key in ['item.specialpickaxes.','tooltip.specialpickaxes.','tooltip.specialpickaxes.secondary.']:self.assertIn(key+id,data)
 def test_no_recipes_ores_worldgen(self):
  self.assertFalse(list((ROOT/'data/specialpickaxes/recipes').glob('*.json')))
  self.assertFalse(list((ROOT/'data/specialpickaxes/advancements/recipes').glob('*.json')))
  self.assertFalse(list(ROOT.glob('data/*/worldgen/**/*.json')))
  self.assertFalse(list(ROOT.glob('assets/specialpickaxes/blockstates/*.json')))
 def test_pickaxe_tag(self):
  for name in ['pickaxes','axes','shovels']:
   tag=json.loads((ROOT/f'data/minecraft/tags/items/{name}.json').read_text())
   self.assertFalse(tag['replace']);self.assertEqual(set(tag['values']),{'specialpickaxes:'+id for id in IDS})
if __name__=='__main__':unittest.main()
