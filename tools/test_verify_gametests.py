"""Regression tests for the CI gate; these do not launch Minecraft or Gradle."""
import pathlib
import tempfile
import unittest

from verify_gametests import expected_test_count, validate_log


class GameTestLogValidation(unittest.TestCase):
    def test_complete_clean_run(self):
        self.assertEqual(validate_log('=== 153 GAME TESTS COMPLETE ===\nAll 153 required tests passed', 153), 153)

    def test_launch_without_summary_is_not_success(self):
        with self.assertRaises(ValueError):
            validate_log('BUILD SUCCESSFUL\nStarting GameTest server', 153)

    def test_partial_run_is_not_success(self):
        with self.assertRaises(ValueError):
            validate_log('=== 152 GAME TESTS COMPLETE ===', 153)

    def test_required_failures_reject_even_a_complete_run(self):
        for failures in ['1 required test failed', '12 required tests failed']:
            with self.subTest(failures=failures), self.assertRaises(ValueError):
                validate_log('=== 153 GAME TESTS COMPLETE ===\n' + failures, 153)

    def test_mixed_summaries_cannot_hide_an_incomplete_run(self):
        with self.assertRaises(ValueError):
            validate_log('=== 152 GAME TESTS COMPLETE ===\n=== 153 GAME TESTS COMPLETE ===', 153)

    def test_empty_source_discovery_is_not_success(self):
        with self.assertRaises(ValueError):
            validate_log('=== 0 GAME TESTS COMPLETE ===', 0)

    def test_discovery_counts_all_test_classes(self):
        with tempfile.TemporaryDirectory() as directory:
            root = pathlib.Path(directory)
            (root / 'FirstGameTests.java').write_text('@GameTest(template="empty") void first() {}')
            (root / 'SecondGameTests.java').write_text('@GameTest(template="empty") void second() {}')
            (root / 'Helper.java').write_text('not a test')
            self.assertEqual(expected_test_count(root), 2)


if __name__ == '__main__':
    unittest.main()
