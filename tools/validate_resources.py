#!/usr/bin/env python3
import json,pathlib,struct,hashlib,unittest
ROOT=pathlib.Path('src/main/resources')
IDS='palimpsest fault_choir eventide meridian paradox_crucible interregnum worldloom icarus hollow_axiom bifold_atlas'.split()
class Resources(unittest.TestCase):
 def test_json(self):
  for pattern in ['*.json','*.mcmeta']:
   for p in ROOT.rglob(pattern):json.loads(p.read_text())
 def test_models_unique_animated_textures(self):
  hashes=set()
  for id in IDS:
   model=json.loads((ROOT/f'assets/specialpickaxes/models/item/{id}.json').read_text())
   ns,path=model['textures']['layer0'].split(':');p=ROOT/f'assets/{ns}/textures/{path}.png';b=p.read_bytes()
   self.assertEqual(b[:8],b'\x89PNG\r\n\x1a\n');self.assertEqual(struct.unpack('>II',b[16:24]),(64,256))
   self.assertEqual(json.loads(p.with_suffix('.png.mcmeta').read_text())['animation']['frametime'],6)
   hashes.add(hashlib.sha256(b).hexdigest())
  self.assertEqual(len(hashes),10)
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
  tag=json.loads((ROOT/'data/minecraft/tags/items/pickaxes.json').read_text())
  self.assertFalse(tag['replace']);self.assertEqual(set(tag['values']),{'specialpickaxes:'+id for id in IDS})
if __name__=='__main__':unittest.main()
