#!/usr/bin/env python3
import hashlib
import json
import pathlib
import re
import struct
import unittest
import zipfile

ROOT = pathlib.Path("src/main/resources")
MANIFEST = pathlib.Path("docs/mining-rework/ASSET-PROVENANCE.json")
from mining_catalog import IDS, MODES


class Resources(unittest.TestCase):
    def test_json(self):
        for pattern in ["*.json", "*.mcmeta"]:
            for path in ROOT.rglob(pattern):
                json.loads(path.read_text())

    def test_reference_models(self):
        hashes = set()
        for artifact in IDS:
            model = json.loads((ROOT / f"assets/specialpickaxes/models/item/{artifact}.json").read_text())
            self.assertEqual(model["textures"]["layer0"], "specialpickaxes:item/" + artifact)
            hashes.add(hashlib.sha256((ROOT / f"assets/specialpickaxes/textures/item/{artifact}.png").read_bytes()).hexdigest())
        self.assertEqual(len(hashes), len(IDS))

    def test_reference_sprite_provenance(self):
        records = json.loads(MANIFEST.read_text())
        self.assertEqual(len(records), len(IDS), "Every pickaxe must have exactly one provenance record")
        self.assertEqual({record.get("artifact") for record in records}, set(IDS))

        for record in records:
            artifact = record["artifact"]
            expected_hash = record.get("sha256", "")
            self.assertRegex(expected_hash, re.compile(r"^[0-9a-f]{64}$"), artifact)

            archive_path = pathlib.Path(record["archive"])
            self.assertFalse(archive_path.is_absolute(), artifact)
            self.assertNotIn("..", archive_path.parts, artifact)
            self.assertTrue(archive_path.is_file(), f"{artifact}: missing reference archive {archive_path}")

            with zipfile.ZipFile(archive_path) as archive:
                try:
                    reference_png = archive.read(record["entry"])
                except KeyError:
                    self.fail(f"{artifact}: missing texture entry {record['entry']} in {archive_path}")

            reference_hash = hashlib.sha256(reference_png).hexdigest()
            self.assertEqual(reference_hash, expected_hash, f"{artifact}: reference archive hash changed")

            runtime_png = (ROOT / f"assets/specialpickaxes/textures/item/{artifact}.png").read_bytes()
            runtime_hash = hashlib.sha256(runtime_png).hexdigest()
            self.assertEqual(runtime_hash, expected_hash, f"{artifact}: runtime texture differs from its reference")

    def test_mining_modes_and_removed_models(self):
        self.assertEqual({path.stem for path in (ROOT / "assets/specialpickaxes/models/item").glob("*.json")}, set(IDS))
        for lang in ["en_us", "es_es"]:
            data = json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            for artifact, modes in MODES.items():
                for mode in modes:
                    self.assertIn(f"mining.identity.{artifact}.{mode}", data)
                    self.assertIn(f"mining.how.{artifact}.{mode}", data)

    def test_current_controls(self):
        for lang in ["en_us", "es_es"]:
            data = json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            self.assertEqual(len([key for key in data if key.startswith("key.specialpickaxes.")]), 7)
            for key in ["message.specialpickaxes.companion_active", "status.specialpickaxes.paused", "message.specialpickaxes.chunk_pause"]:
                self.assertNotIn("agachado", data[key].lower())
                self.assertNotIn("sneak", data[key].lower())
                self.assertNotIn("usar", data[key].lower().replace("pausar", ""))

    def test_no_energy_and_manual_keys(self):
        for lang in ["en_us", "es_es"]:
            data = json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            self.assertFalse(any("energy" in key for key in data))
            for artifact in IDS:
                for prefix in ["identity.", "controls.", "example."]:
                    self.assertIn(prefix + "specialpickaxes." + artifact, data)

    def test_languages(self):
        languages = [json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text()) for lang in ["en_us", "es_es"]]
        self.assertEqual(set(languages[0]), set(languages[1]))
        for data in languages:
            for artifact in IDS:
                for key in ["item.specialpickaxes.", "tooltip.specialpickaxes.", "tooltip.specialpickaxes.secondary."]:
                    self.assertIn(key + artifact, data)

    def test_no_recipes_ores_worldgen(self):
        self.assertFalse(list((ROOT / "data/specialpickaxes/recipes").glob("*.json")))
        self.assertFalse(list((ROOT / "data/specialpickaxes/advancements/recipes").glob("*.json")))
        self.assertFalse(list(ROOT.glob("data/*/worldgen/**/*.json")))
        self.assertFalse(list(ROOT.glob("assets/specialpickaxes/blockstates/*.json")))

    def test_pickaxe_tag(self):
        for name in ["pickaxes", "axes", "shovels"]:
            tag = json.loads((ROOT / f"data/minecraft/tags/items/{name}.json").read_text())
            self.assertFalse(tag["replace"])
            self.assertEqual(set(tag["values"]), {"specialpickaxes:" + artifact for artifact in IDS})


if __name__ == "__main__":
    unittest.main()
