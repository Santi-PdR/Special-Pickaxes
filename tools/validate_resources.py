#!/usr/bin/env python3
import json,pathlib,struct,hashlib,unittest
ROOT=pathlib.Path('src/main/resources')
IDS='palimpsest fault_choir eventide meridian paradox_crucible interregnum worldloom icarus hollow_axiom bifold_atlas worldbreaker chronicle keystone tessellator aegis lodestar seam_ripper causeway counterseal covenant'.split()
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
  self.assertEqual(len(hashes),20)
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
