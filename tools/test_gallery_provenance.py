import hashlib,json,pathlib,tempfile,unittest
from unittest.mock import patch
from gallery_provenance import validate_gallery
from mining_catalog import IDS


class GalleryProvenanceTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.addCleanup(self.temp.cleanup)
        self.root=pathlib.Path(self.temp.name);self.captures=self.root/'build/client-smoke';self.captures.mkdir(parents=True)
        self.assets=self.root/'assets';self.blobs={};self.manifest=[]
        self.release={'file':'test.jar','commit':'a'*40,'run_id':'123','artifacts':IDS}
        self.write_evidence(self.root/'build/RELEASE.json',json.dumps(self.release).encode())
        for name in IDS:
            # Unit fixtures are bytes, never gallery deliverables or fabricated in-game captures.
            model=self.assets/'models/item'/f'{name}.json';model.parent.mkdir(parents=True,exist_ok=True)
            model.write_text(json.dumps({'textures':{'layer0':'specialpickaxes:item/'+name}}));self.blobs[model]=model.read_bytes()
            icon=self.assets/'textures/item'/f'{name}.png';icon.parent.mkdir(parents=True,exist_ok=True);icon.write_bytes(name.encode());self.blobs[icon]=icon.read_bytes()
            self.write_evidence(self.captures/f'portrait-{name}.jpg',('unit-fixture-'+name).encode())
        self.save_manifest()
    def write_evidence(self,path,data):
        path.write_bytes(data);self.manifest.append({'path':path.relative_to(self.root).as_posix(),'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest()})
    def save_manifest(self):
        (self.root/'manifest.json').write_text(json.dumps(self.manifest))
    def validate(self):
        return validate_gallery(self.captures,read_at_commit=lambda commit,path:self.blobs[path],assets=self.assets)
    def test_valid_gallery_records_all_thirteen_assets_and_portraits(self):
        result=self.validate();self.assertEqual(len(result['items']),13);self.assertEqual(result['commit'],'a'*40)
        self.assertEqual(result['items'][0]['artifact'],IDS[0])
    def test_icon_from_a_different_version_is_rejected(self):
        (self.assets/'textures/item/palimpsest.png').write_bytes(b'new icon')
        with self.assertRaisesRegex(ValueError,'icon does not match'):self.validate()
    def test_model_from_a_different_version_is_rejected(self):
        (self.assets/'models/item/palimpsest.json').write_text('{}')
        with self.assertRaisesRegex(ValueError,'model does not match'):self.validate()
    def test_modified_portrait_is_rejected(self):
        (self.captures/'portrait-palimpsest.jpg').write_bytes(b'changed')
        with self.assertRaisesRegex(ValueError,'Modified gallery evidence'):self.validate()
    def test_missing_portrait_manifest_is_rejected(self):
        self.manifest=self.manifest[:-1];self.save_manifest()
        with self.assertRaisesRegex(ValueError,'Missing verified gallery evidence'):self.validate()
    def test_modified_release_is_rejected(self):
        self.release['run_id']='456';(self.root/'build/RELEASE.json').write_text(json.dumps(self.release))
        with self.assertRaisesRegex(ValueError,'Modified gallery evidence'):self.validate()
    def test_abbreviated_commit_is_rejected(self):
        self.release['commit']='aaaaaaa';(self.root/'build/RELEASE.json').write_text(json.dumps(self.release))
        with self.assertRaisesRegex(ValueError,'full source commit'):self.validate()
    def test_duplicate_evidence_manifest_entry_is_rejected(self):
        self.manifest.append(self.manifest[-1]);self.save_manifest()
        with self.assertRaisesRegex(ValueError,'Duplicate gallery evidence'):self.validate()
