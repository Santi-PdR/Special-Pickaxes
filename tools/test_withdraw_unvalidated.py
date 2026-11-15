import json
import subprocess
import unittest
from unittest.mock import patch
from withdraw_unvalidated import needs_withdrawal, gh_json, main, WITHDRAWN


class LegacyWithdrawalTests(unittest.TestCase):
    def test_already_withdrawn_output_is_not_rewritten(self):
        self.assertFalse(needs_withdrawal({'name': 'Build evidence 01/03', 'output': dict(WITHDRAWN)}))

    def test_real_binary_or_partial_marker_still_requires_withdrawal(self):
        self.assertTrue(needs_withdrawal({'name': 'Build evidence 01/03', 'output': {'text': 'legacy binary'}}))
        self.assertTrue(needs_withdrawal({'name': 'Build evidence 01/03', 'output': {'title': WITHDRAWN['title']}}))
        self.assertTrue(needs_withdrawal({'name': 'Build evidence 01/03', 'output': None}))

    def test_unrelated_checks_are_never_modified(self):
        for name in ('forge', 'Forge diagnostics', 'Live client diagnostics', ''):
            self.assertFalse(needs_withdrawal({'name': name, 'output': {}}))

    def test_transient_failure_retries_but_authentication_failure_does_not(self):
        transient = subprocess.CompletedProcess([], 1, '', 'HTTP 503 service unavailable')
        success = subprocess.CompletedProcess([], 0, json.dumps({'ok': True}), '')
        with patch('withdraw_unvalidated.subprocess.run', side_effect=[transient, success]) as run, patch('withdraw_unvalidated.time.sleep') as sleep:
            self.assertEqual(gh_json('unit-fixture'), {'ok': True})
            self.assertEqual(run.call_count, 2)
            sleep.assert_called_once_with(5)
        denied = subprocess.CompletedProcess([], 1, '', 'HTTP 401 Bad credentials')
        with patch('withdraw_unvalidated.subprocess.run', return_value=denied) as run, patch('withdraw_unvalidated.time.sleep') as sleep:
            with self.assertRaisesRegex(RuntimeError, 'withdrawal failed'):
                gh_json('unit-fixture')
            self.assertEqual(run.call_count, 1)
            sleep.assert_not_called()

    def test_repeated_transient_failure_still_blocks_the_run(self):
        failed = subprocess.CompletedProcess([], 1, '', 'HTTP 429 secondary rate limit')
        with patch('withdraw_unvalidated.subprocess.run', return_value=failed) as run, patch('withdraw_unvalidated.time.sleep'):
            with self.assertRaisesRegex(RuntimeError, 'withdrawal failed'):
                gh_json('unit-fixture', {'output': WITHDRAWN})
            self.assertEqual(run.call_count, 3)

    def test_pages_after_the_first_are_checked_and_only_old_output_is_patched(self):
        pages = [{'check_runs': [{'id': 101, 'name': 'Build evidence 01/01', 'output': dict(WITHDRAWN)}]},
                 {'check_runs': [{'id': 102, 'name': 'Build evidence 01/01', 'output': {'text': 'legacy'}}]}]
        with patch('withdraw_unvalidated.LEGACY_COMMITS', ('unit-fixture',)), patch('withdraw_unvalidated.gh_json', side_effect=[pages, {}]) as api, patch('withdraw_unvalidated.Path'), patch('builtins.print'):
            main()
            self.assertEqual(api.call_count, 2)
            self.assertTrue(api.call_args_list[0].kwargs['paginate'])
            self.assertTrue(api.call_args_list[1].args[0].endswith('/check-runs/102'))
            self.assertEqual(api.call_args_list[1].args[1], {'output': WITHDRAWN})

    def test_paginated_reads_support_adjacent_documents_from_older_gh(self):
        success = subprocess.CompletedProcess([], 0, '{"check_runs": []}\n{"check_runs": []}', '')
        with patch('withdraw_unvalidated.subprocess.run', return_value=success) as run:
            self.assertEqual(gh_json('unit-fixture', paginate=True), [{'check_runs': []}, {'check_runs': []}])
            self.assertIn('--paginate', run.call_args.args[0])
            self.assertNotIn('--slurp', run.call_args.args[0])
