#!/usr/bin/env python3
import json, pathlib, struct, unittest
ROOT=pathlib.Path('src/main/resources')
IDS='overdrive excavator vein_miner inferno magnetic scanner storm void ender explosive'.split()
class Resources(unittest.TestCase):
 def test_all_json(self):
  for p in ROOT.rglob('*.json'):json.loads(p.read_text())
 def test_models_textures_recipes_localization(self):
  for name in IDS:
   model=json.loads((ROOT/f'assets/specialpickaxes/models/item/{name}.json').read_text())
   namespace,path=model['textures']['layer0'].split(':')
   texture=(ROOT/f'assets/{namespace}/textures/{path}.png').read_bytes()
   self.assertEqual(texture[:8],b'\x89PNG\r\n\x1a\n')
   self.assertEqual(struct.unpack('>II',texture[16:24]),(16,16))
   recipe=json.loads((ROOT/f'data/specialpickaxes/recipes/{name}.json').read_text())
   self.assertEqual(recipe['result']['item'],'specialpickaxes:'+name)
   self.assertEqual(set(''.join(recipe['pattern']))-{' '},set(recipe['key']))
   for lang in ['en_us','es_es']:
    data=json.loads((ROOT/f'assets/specialpickaxes/lang/{lang}.json').read_text())
    self.assertIn('item.specialpickaxes.'+name,data)
    self.assertIn('tooltip.specialpickaxes.'+name,data)
 def test_languages_match(self):
  langs=[json.loads((ROOT/f'assets/specialpickaxes/lang/{lang}.json').read_text()) for lang in ['en_us','es_es']]
  self.assertEqual(set(langs[0]),set(langs[1]))
 def test_loot_modifier(self):
  data=json.loads((ROOT/'data/forge/loot_modifiers/global_loot_modifiers.json').read_text())
  self.assertFalse(data['replace'])
  self.assertEqual(data['entries'],['specialpickaxes:auto_smelt'])
if __name__=='__main__':unittest.main()
