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

    def test_missing_log_and_directory_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            self.assertEqual(len(scan_logs([pathlib.Path(directory) / 'absent.log', directory])), 2)


if __name__ == '__main__':
    unittest.main()
