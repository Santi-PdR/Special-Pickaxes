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

    def test_iridium_lode_shard_particle_atlas(self):
        particle = json.loads((ROOT / "assets/specialpickaxes/particles/iridium_lode_shard.json").read_text())
        self.assertEqual(particle["textures"], ["specialpickaxes:iridium_lode_shard"])
        animation = json.loads((ROOT / "assets/specialpickaxes/textures/particle/iridium_lode_shard.png.mcmeta").read_text())["animation"]
        self.assertEqual(animation["frames"], list(range(8)))
        self.assertEqual(animation["frametime"], 2)
        header = (ROOT / "assets/specialpickaxes/textures/particle/iridium_lode_shard.png").read_bytes()[:24]
        self.assertEqual(struct.unpack(">II", header[16:24]), (32, 256), "Blockbench atlas must be eight vertically stacked 32x32 frames")

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

    def test_choir_manual_matches_live_tunnel(self):
        for lang in ["en_us", "es_es"]:
            data = json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            self.assertIn("2×2", data["identity.specialpickaxes.fault_choir"])
            self.assertIn("20", data["identity.specialpickaxes.fault_choir"])
            self.assertIn("2×2", data["play4.fault_choir"])
            self.assertIn("20", data["play4.fault_choir"])
            self.assertNotIn("replay", data["identity.specialpickaxes.fault_choir"].lower())
            self.assertNotIn("repite", data["identity.specialpickaxes.fault_choir"].lower())
            combat_term = "Fault Resonance" if lang == "en_us" else "Resonancia de Falla"
            self.assertIn(combat_term, data["combat.specialpickaxes.fault_choir"])
            self.assertFalse(any(key.startswith(("mining.identity.fault_choir.rotate", "mining.identity.fault_choir.mirror",
                                                 "mining.how.fault_choir.rotate", "mining.how.fault_choir.mirror"))
                                 for key in data))

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
                    self.assertTrue(data[prefix + artifact].strip(), f"{lang}: empty {prefix + artifact}")

    def test_playable_pickaxe_manual_coverage(self):
        required_prefixes = [
            "item.specialpickaxes.",
            "tooltip.specialpickaxes.",
            "tooltip.specialpickaxes.secondary.",
            "mining.short.specialpickaxes.",
            "alternate.specialpickaxes.",
            "alternate.detail.specialpickaxes.",
            "melee.specialpickaxes.",
            "passive.specialpickaxes.",
            "curios.detail.specialpickaxes.",
        ]
        locales = {
            lang: json.loads((ROOT / f"assets/specialpickaxes/lang/{lang}.json").read_text())
            for lang in ["en_us", "es_es"]
        }
        for artifact in IDS:
            for lang, data in locales.items():
                for prefix in required_prefixes:
                    key = prefix + artifact
                    self.assertIn(key, data, f"{lang}: missing {key}")
                    self.assertTrue(data[key].strip(), f"{lang}: empty {key}")

                for mode in MODES[artifact]:
                    for key in [
                        f"mining.identity.{artifact}.{mode}",
                        f"mining.how.{artifact}.{mode}",
                        f"mode.specialpickaxes.{mode}",
                    ]:
                        self.assertIn(key, data, f"{lang}: missing {key}")
                        self.assertTrue(data[key].strip(), f"{lang}: empty {key}")

        self.assertEqual(set(locales["en_us"]), set(locales["es_es"]))

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

    def test_curios_pickaxe_slot_registration(self):
        slot = json.loads((ROOT / "data/specialpickaxes/curios/slots/pickaxe.json").read_text())
        self.assertEqual(slot["size"], 1)
        self.assertEqual(slot["icon"], "specialpickaxes:slot/empty_pickaxe_slot")
        self.assertEqual(slot["validators"], ["curios:tag"])
        self.assertFalse(slot["add_cosmetic"])

        entities = json.loads((ROOT / "data/specialpickaxes/curios/entities/entities.json").read_text())
        self.assertEqual(entities["entities"], ["minecraft:player"])
        self.assertIn("pickaxe", entities["slots"])

        tag = json.loads((ROOT / "data/curios/tags/items/pickaxe.json").read_text())
        self.assertFalse(tag["replace"])
        self.assertEqual(set(tag["values"]), {"specialpickaxes:" + artifact for artifact in IDS})

        dependency = re.search(r'\[\[dependencies\.specialpickaxes\]\]\s*modId="curios"\s*mandatory=(true|false)',
                               pathlib.Path("src/main/resources/META-INF/mods.toml").read_text())
        self.assertIsNotNone(dependency, "Curios must remain optional")
        self.assertEqual(dependency.group(1), "false")


if __name__ == "__main__":
    unittest.main()
