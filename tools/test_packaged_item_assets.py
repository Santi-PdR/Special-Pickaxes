import hashlib,io,json,pathlib,unittest,zipfile
from packaged_item_assets import ASSETS,IDS,MANIFEST,verify_packaged_item_assets


class PackagedItemAssetTests(unittest.TestCase):
    def setUp(self):
        self.files={}
        for name in IDS:
            model=f'models/item/{name}.json';self.files[model]=(ASSETS/model).read_bytes()
            texture='textures/'+json.loads(self.files[model])['textures']['layer0'].split(':',1)[1]+'.png'
            self.files[texture]=(ASSETS/texture).read_bytes()
            if (ASSETS/(texture+'.mcmeta')).exists():self.files[texture+'.mcmeta']=(ASSETS/(texture+'.mcmeta')).read_bytes()
        self.records=json.loads(MANIFEST.read_text())
    def verify(self):
        data=io.BytesIO()
        with zipfile.ZipFile(data,'w') as archive:
            for path,content in self.files.items():archive.writestr('assets/specialpickaxes/'+path,content)
        with zipfile.ZipFile(io.BytesIO(data.getvalue())) as archive:return verify_packaged_item_assets(archive,records=self.records)
    def test_every_runtime_model_texture_and_animation_is_exact(self):self.assertEqual(self.verify(),IDS)
    def test_old_cached_model_is_rejected_even_if_valid_json(self):
        self.files['models/item/worldloom.json']=b'{"parent":"minecraft:item/handheld","textures":{"layer0":"specialpickaxes:item/worldloom"}}'
        with self.assertRaisesRegex(ValueError,'Stale or modified'):self.verify()
    def test_old_cached_icon_is_rejected_even_if_it_is_a_valid_png(self):
        self.files['textures/item/worldloom_v2.png']=(ASSETS/'textures/item/worldloom.png').read_bytes()
        with self.assertRaisesRegex(ValueError,'Stale or modified'):self.verify()
    def test_missing_runtime_texture_is_rejected(self):
        del self.files['textures/item/worldloom_v2.png']
        with self.assertRaisesRegex(ValueError,'Missing packaged item resource'):self.verify()
    def test_wrong_provenance_digest_is_rejected(self):
        self.records[0]['runtime_sha256']='0'*64
        with self.assertRaisesRegex(ValueError,'provenance mismatch'):self.verify()
    def test_unexpected_animation_metadata_is_rejected(self):
        self.files['textures/item/worldloom_v2.png.mcmeta']=b'{}'
        with self.assertRaisesRegex(ValueError,'Unexpected animation metadata'):self.verify()
    def test_incomplete_provenance_is_rejected(self):
        self.records.pop()
        with self.assertRaisesRegex(ValueError,'exactly thirteen'):self.verify()
    def test_duplicate_zip_entries_are_rejected(self):
        import warnings
        data=io.BytesIO()
        with warnings.catch_warnings():
            warnings.simplefilter('ignore',UserWarning)
            with zipfile.ZipFile(data,'w') as archive:archive.writestr('duplicate',b'1');archive.writestr('duplicate',b'2')
        with zipfile.ZipFile(io.BytesIO(data.getvalue())) as archive:
            with self.assertRaisesRegex(ValueError,'Duplicate ZIP entries'):verify_packaged_item_assets(archive,records=self.records)
