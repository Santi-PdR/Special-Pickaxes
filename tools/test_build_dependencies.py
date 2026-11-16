"""Source contracts only: do not claim Maven availability or Gradle execution."""
from pathlib import Path
import unittest


class BuildDependencyContracts(unittest.TestCase):
    def test_forgegradle_uses_the_exact_observed_implementation_version(self):
        build = Path('build.gradle').read_text()
        self.assertIn("id 'net.minecraftforge.gradle' version '6.0.54'", build)
        settings = Path('settings.gradle').read_text()
        self.assertIn('net.minecraftforge.gradle:ForgeGradle:${requested.version}', settings)

    def test_central_excludes_forge_and_synthetic_minecraft_coordinates(self):
        build = Path('build.gradle').read_text()
        self.assertIn("url = 'https://repo1.maven.org/maven2/'", build)
        self.assertIn("excludeGroup 'net.minecraft'", build)
        self.assertIn("excludeGroup 'cpw.mods'", build)
        self.assertIn("excludeGroupByRegex 'net\\\\.minecraftforge(\\\\..*)?'", build)

    def test_plugin_repositories_remain_official_and_tls_enabled(self):
        settings = Path('settings.gradle').read_text()
        self.assertIn("https://maven.minecraftforge.net/", settings)
        self.assertNotIn('allowInsecureProtocol', settings)
        self.assertNotIn('http://', settings)
