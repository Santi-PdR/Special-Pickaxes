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
            self.assertTrue(model["textures"]["layer0"].startswith("specialpickaxes:item/"))
            texture_name = model["textures"]["layer0"].split(":", 1)[1]
            texture_path = ROOT / "assets/specialpickaxes/textures" / f"{texture_name}.png"
            self.assertTrue(texture_path.is_file(), f"{artifact}: missing model texture {texture_path}")
            hashes.add(hashlib.sha256(texture_path.read_bytes()).hexdigest())
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

            runtime_texture = record.get("runtime_texture", f"{artifact}.png")
            runtime_expected = record.get("runtime_sha256", expected_hash)
            self.assertRegex(runtime_expected, re.compile(r"^[0-9a-f]{64}$"), artifact)
            runtime_png = (ROOT / f"assets/specialpickaxes/textures/item/{runtime_texture}").read_bytes()
            runtime_hash = hashlib.sha256(runtime_png).hexdigest()
            self.assertEqual(runtime_hash, runtime_expected, f"{artifact}: runtime texture hash differs from provenance")

    def test_item_texture_dimensions(self):
        models = ROOT / "assets/specialpickaxes/models/item"
        for path in models.glob("*.json"):
            model = json.loads(path.read_text())
            texture_name = model["textures"]["layer0"].split(":", 1)[1]
            png = ROOT / "assets/specialpickaxes/textures" / f"{texture_name}.png"
            data = png.read_bytes()
            self.assertTrue(data.startswith(b"\x89PNG\r\n\x1a\n"), path.stem)
            width, height = struct.unpack(">II", data[16:24])
            self.assertEqual(width, height, f"{path.stem}: texture must be square")
            self.assertIn((width, height), {(16, 16), (32, 32)}, f"{path.stem}: unsupported icon size")

    def test_mining_modes_and_removed_models(self):
        self.assertEqual({path.stem for path in (ROOT / "assets/specialpickaxes/models/item").glob("*.json")}, set(IDS))
        for lang in ["en_us", "es_es"]:
            data = json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            for artifact, modes in MODES.items():
                for mode in modes:
                    self.assertIn(f"mining.identity.{artifact}.{mode}", data)
                    self.assertIn(f"mining.how.{artifact}.{mode}", data)

    def test_current_controls(self):
        source = pathlib.Path("src/main/java/io/github/santipdr/specialpickaxes/client/RelicKeys.java").read_text()
        actions_source = pathlib.Path("src/main/java/io/github/santipdr/specialpickaxes/artifact/RelicControl.java").read_text()
        actions = set(re.search(r"enum Action \{([^}]+)\}", actions_source).group(1).replace(" ", "").split(","))
        register_body = re.search(r"void register\([^)]*\)\s*\{([^}]+)\}", source).group(1)
        excluded = set(re.findall(r"action!=RelicControl\.Action\.(\w+)", register_body))
        registered = {"key.specialpickaxes." + action.lower() for action in actions - excluded}
        registered.add("key.specialpickaxes.manual")
        self.assertEqual(registered, {"key.specialpickaxes.activate", "key.specialpickaxes.mode", "key.specialpickaxes.alt_skill", "key.specialpickaxes.manual"})
        for lang in ["en_us", "es_es"]:
            data = json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            self.assertTrue(registered <= data.keys())
            for key in ["message.specialpickaxes.companion_active", "status.specialpickaxes.paused", "message.specialpickaxes.chunk_pause"]:
                self.assertNotIn("agachado", data[key].lower())
                self.assertNotIn("sneak", data[key].lower())
                self.assertNotIn("usar", data[key].lower().replace("pausar", ""))

    def test_curios_pickaxe_slot(self):
        slot = json.loads((ROOT / "data/specialpickaxes/curios/slots/pickaxe.json").read_text())
        self.assertEqual(slot.get("size"), 1)
        self.assertEqual(slot.get("icon"), "specialpickaxes:slot/empty_pickaxe_slot")
        self.assertFalse(slot.get("add_cosmetic"))
        self.assertIn("curios:tag", slot.get("validators", []))
        self.assertTrue((ROOT / "assets/specialpickaxes/textures/slot/empty_pickaxe_slot.png").is_file())

        entities = json.loads((ROOT / "data/specialpickaxes/curios/entities/entities.json").read_text())
        self.assertIn("minecraft:player", entities.get("entities", []))
        self.assertIn("pickaxe", entities.get("slots", []))

        tag = json.loads((ROOT / "data/curios/tags/items/pickaxe.json").read_text())
        self.assertFalse(tag.get("replace"))
        self.assertEqual(set(tag.get("values", [])), {"specialpickaxes:" + artifact for artifact in IDS})
        for lang in ["en_us", "es_es"]:
            data = json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            self.assertIn("curios.identifier.pickaxe", data)

    def test_no_energy_and_manual_keys(self):
        kind_source = pathlib.Path("src/main/java/io/github/santipdr/specialpickaxes/artifact/ArtifactKind.java").read_text()
        kind_ids = dict(re.findall(r'^\s*([A-Z_]+)\("([^"]+)"', kind_source, re.M))
        playable_case = re.search(r'playable\(\)\{return switch\(this\)\{case ([^}]+?)\->true;', kind_source).group(1)
        playable = {kind_ids[kind] for kind in re.findall(r"\b[A-Z_]+\b", playable_case) if kind in kind_ids}
        self.assertEqual(set(IDS), playable)
        for lang in ["en_us", "es_es"]:
            data = json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            self.assertFalse(any("energy" in key for key in data))
            for artifact in IDS:
                for prefix in ["item.specialpickaxes.", "tooltip.specialpickaxes.", "tooltip.specialpickaxes.secondary.", "alternate.detail.specialpickaxes.", "melee.specialpickaxes.", "passive.specialpickaxes.", "curios.detail.specialpickaxes."]:
                    self.assertIn(prefix + artifact, data)

    def test_languages(self):
        languages = [json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text()) for lang in ["en_us", "es_es"]]
        self.assertEqual(set(languages[0]), set(languages[1]))
        for data in languages:
            for artifact in IDS:
                for key in ["item.specialpickaxes.", "tooltip.specialpickaxes.", "tooltip.specialpickaxes.secondary."]:
                    self.assertIn(key + artifact, data)

    def test_no_recipes_ores_worldgen(self):
        recipe_json = [path for path in (ROOT / "data").rglob("*.json") if "recipes" in path.parts]
        self.assertFalse(recipe_json, f"Pickaxes must remain recipe-free; found recipe files: {recipe_json}")
        self.assertFalse(list(ROOT.glob("data/*/worldgen/**/*.json")))
        self.assertFalse(list(ROOT.glob("assets/specialpickaxes/blockstates/*.json")))

    def test_pickaxe_tag(self):
        for name in ["pickaxes", "axes", "shovels"]:
            tag = json.loads((ROOT / f"data/minecraft/tags/items/{name}.json").read_text())
            self.assertFalse(tag["replace"])
            self.assertEqual(set(tag["values"]), {"specialpickaxes:" + artifact for artifact in IDS})


if __name__ == "__main__":
    unittest.main()
