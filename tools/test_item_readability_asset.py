import json,pathlib,tempfile,unittest
from item_readability_asset import ROOT,VARIANTS,read_rgba,readable,write_png

class ItemReadabilityTests(unittest.TestCase):
    def test_alpha_and_silhouette_are_preserved_exactly_on_two_pixel_grid(self):
        for name in VARIANTS:
            old=read_rgba(ROOT/'textures/item'/f'{name}.png');new=read_rgba(ROOT/'textures/item'/f'{name}_v2.png')
            self.assertEqual((len(new),len(new[0])),(32,32))
            for y,row in enumerate(new):
                for x,pixel in enumerate(row):self.assertEqual(pixel[3],old[y//2][x//2][3],name)
    def test_visible_channels_never_darken_and_some_shadows_improve(self):
        for name in VARIANTS:
            old=read_rgba(ROOT/'textures/item'/f'{name}.png');new=read_rgba(ROOT/'textures/item'/f'{name}_v2.png');improved=0
            for y,row in enumerate(old):
                for x,pixel in enumerate(row):
                    if not pixel[3]:continue
                    result=new[y*2][x*2]
                    self.assertTrue(all(result[c]>=pixel[c] for c in range(3)),name)
                    improved+=result[:3]!=pixel[:3]
            self.assertGreater(improved,10,name)
    def test_runtime_variants_reproduce_byte_for_byte(self):
        with tempfile.TemporaryDirectory() as directory:
            for name,gamma in VARIANTS.items():
                rows=readable(read_rgba(ROOT/'textures/item'/f'{name}.png'),gamma);output=pathlib.Path(directory)/'test.png'
                write_png(output,rows,32,32)
                self.assertEqual(output.read_bytes(),(ROOT/'textures/item'/f'{name}_v2.png').read_bytes())
    def test_runtime_models_use_variants_not_the_preserved_sources(self):
        for name in VARIANTS:
            model=json.loads((ROOT/'models/item'/f'{name}.json').read_text())
            self.assertEqual(model['textures']['layer0'],'specialpickaxes:item/'+name+'_v2')
    def test_invalid_gamma_is_rejected(self):
        for gamma in (-1,0,2):
            with self.assertRaises(ValueError):readable([[(1,2,3,4)]],gamma)
