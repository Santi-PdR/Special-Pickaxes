"""Regression tests for the CI gate; these do not launch Minecraft or Gradle."""
import pathlib
import tempfile
import unittest

from verify_gametests import expected_test_count, validate_log


class GameTestLogValidation(unittest.TestCase):
    def test_complete_clean_run(self):
        self.assertEqual(validate_log('=== 153 GAME TESTS COMPLETE ===\nAll 153 required tests passed', 153), 153)

    def test_midnight_rotation_requires_full_transcript_not_shutdown_only_latest(self):
        before = '[23:59:55] === 525 GAME TESTS COMPLETE ===\nAll 525 required tests passed\n'
        after = '[00:00:10] Saving chunks\nBUILD SUCCESSFUL'
        with self.assertRaises(ValueError):
            validate_log(after, 525)
        self.assertEqual(validate_log(before + after, 525), 525)
        workflow = pathlib.Path('.github/workflows/verify.yml').read_text()
        self.assertIn('python3 tools/verify_gametests.py build/verification-gametest.log', workflow)
        self.assertIn('python3 tools/validate_runtime_logs.py run/logs/gametest.log build/verification-gametest.log', workflow)

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

    def test_complete_count_without_success_confirmation_is_rejected(self):
        for suffix in ('', '\nAll 152 required tests passed', '\nAll 154 required tests passed'):
            with self.subTest(suffix=suffix), self.assertRaises(ValueError):
                validate_log('=== 153 GAME TESTS COMPLETE ==='+suffix, 153)

    def test_individual_failure_cannot_be_hidden_by_a_success_summary(self):
        for failure in ('[Server thread/ERROR] broken failed!', 'broken failed!'):
            with self.subTest(failure=failure), self.assertRaises(ValueError):
                validate_log(failure+'\n=== 153 GAME TESTS COMPLETE ===\nAll 153 required tests passed', 153)

    def test_contradictory_success_summaries_are_rejected(self):
        with self.assertRaises(ValueError):
            validate_log('=== 153 GAME TESTS COMPLETE ===\nAll 152 required tests passed\nAll 153 required tests passed', 153)

    def test_discovery_counts_all_test_classes(self):
        with tempfile.TemporaryDirectory() as directory:
            root = pathlib.Path(directory)
            (root / 'FirstGameTests.java').write_text('@GameTest(template="empty") void first() {}')
            (root / 'SecondGameTests.java').write_text('@GameTest(template="empty") void second() {}')
            (root / 'Helper.java').write_text('not a test')
            self.assertEqual(expected_test_count(root), 2)


if __name__ == '__main__':
    unittest.main()
