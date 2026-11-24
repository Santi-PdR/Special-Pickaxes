"""Negative runtime-log tests: Gradle exit zero is never sufficient proof."""
import pathlib
import tempfile
import unittest

from validate_runtime_logs import scan_logs


class RuntimeLogValidation(unittest.TestCase):
    def scan(self, text):
        with tempfile.TemporaryDirectory() as directory:
            log = pathlib.Path(directory) / 'latest.log'
            log.write_text(text)
            return scan_logs([log])

    def test_clean_start_stop_is_accepted(self):
        self.assertEqual(self.scan('[Server thread/INFO] Done (4.1s)!\nSaving chunks\nStopping server'), [])

    def test_qualified_and_unqualified_java_failures_are_rejected(self):
        for line in ['java.lang.NullPointerException: target', 'IllegalStateException: bad state',
                     'Caused by: java.lang.NoClassDefFoundError: missing', 'java.lang.AssertionError',
                     '[Render thread/ERROR] Unable to bake texture']:
            with self.subTest(line=line):
                self.assertEqual(len(self.scan(line)), 1)

    def test_vanilla_world_generation_errors_are_not_silenced(self):
        self.assertTrue(self.scan('[Worker-Main-1/ERROR] [minecraft/MonsterRoomFeature]: Failed to fetch mob spawner entity at (-12, -11, -109)'))

    def test_packaged_boot_fixture_is_reproducible_without_worldgen_decorations(self):
        import ast
        import json
        source = pathlib.Path(__file__).with_name('dedicated_smoke.py').read_text()
        tree = ast.parse(source)
        properties = next(node.args[0].value for node in ast.walk(tree)
                          if isinstance(node, ast.Call) and isinstance(node.func, ast.Attribute)
                          and node.func.attr == 'write_text' and node.args
                          and isinstance(node.args[0], ast.Constant)
                          and isinstance(node.args[0].value, str)
                          and 'level-name=smoke-world' in node.args[0].value)
        values = dict(line.split('=', 1) for line in properties.splitlines() if '=' in line)
        self.assertEqual(values['level-seed'], '749123')
        self.assertEqual(values['level-type'], 'minecraft:flat')
        self.assertEqual(values['generate-structures'], 'false')
        settings = json.loads(values['generator-settings'])
        self.assertFalse(settings['features'])
        self.assertFalse(settings['lakes'])
        self.assertEqual(settings['structure_overrides'], [])
        self.assertEqual(settings['biome'], 'minecraft:plains')
        self.assertGreater(len(settings['layers']), 0)

    def test_unknown_items_and_commands_are_rejected(self):
        for line in ['Unknown item specialpickaxes:broken', 'Unknown command at position 0']:
            self.assertTrue(self.scan(line))

    def test_python_tracebacks_are_rejected(self):
        self.assertTrue(self.scan('Traceback (most recent call last):'))

    def test_line_numbers_and_multiple_failures_survive(self):
        issues = self.scan('Starting\njava.lang.IllegalStateException: bad\nUnknown item broken')
        self.assertEqual(len(issues), 2)
        self.assertIn(':2:', issues[0])
        self.assertIn(':3:', issues[1])

    def test_empty_or_whitespace_only_logs_are_not_evidence(self):
        for text in ('', '  \n\t'):
            with self.subTest(text=text):
                self.assertEqual(len(self.scan(text)), 1)

    def test_missing_log_and_directory_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            self.assertEqual(len(scan_logs([pathlib.Path(directory) / 'absent.log', directory])), 2)


if __name__ == '__main__':
    unittest.main()
