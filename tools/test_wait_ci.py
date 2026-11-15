import json
import subprocess
import unittest
from unittest.mock import patch

from wait_ci import validate_run, wait_for_run, read_run

SHA = 'a' * 40
RUN_ID = 123


def run(status='completed', conclusion='success', **extra):
    return dict({'id': RUN_ID, 'head_sha': SHA, 'status': status, 'conclusion': conclusion}, **extra)


class CIWaitTests(unittest.TestCase):
    def test_only_explicit_success_returns_zero(self):
        self.assertEqual(validate_run(run(), RUN_ID, SHA), 0)
        for conclusion in ('failure', 'cancelled', 'timed_out', 'action_required', 'neutral', 'skipped', 'stale'):
            with self.subTest(conclusion=conclusion):
                self.assertEqual(validate_run(run(conclusion=conclusion), RUN_ID, SHA), 1)

    def test_unfinished_run_is_not_success(self):
        for status in ('queued', 'in_progress', 'waiting', 'pending', 'requested'):
            self.assertIsNone(validate_run(run(status=status, conclusion=None), RUN_ID, SHA))

    def test_wrong_run_or_source_cannot_be_accepted(self):
        for response in (None, [], {}, run(id=456), run(head_sha='b' * 40), run(id=True)):
            with self.subTest(response=response), self.assertRaises(ValueError):
                validate_run(response, RUN_ID, SHA)

    def test_missing_conclusion_and_unknown_status_fail_closed(self):
        for response in (run(conclusion=None), run(conclusion=''), run(status='unexpected')):
            with self.assertRaises(ValueError):
                validate_run(response, RUN_ID, SHA)

    def test_metadata_query_never_fetches_jobs_or_checks(self):
        result = subprocess.CompletedProcess([], 0, json.dumps(run()), '')
        with patch('wait_ci.subprocess.run', return_value=result) as call:
            self.assertEqual(read_run(RUN_ID), run())
            self.assertEqual(call.call_args.args[0][-1], 'repos/Santi-PdR/Special-Pickaxes/actions/runs/123')

    def test_quota_and_authentication_errors_are_not_retried(self):
        for error in ('HTTP 403 API rate limit exceeded', 'HTTP 401 Bad credentials'):
            result = subprocess.CompletedProcess([], 1, '', error)
            with patch('wait_ci.subprocess.run', return_value=result) as call, self.assertRaises(RuntimeError):
                read_run(RUN_ID)
            self.assertEqual(call.call_count, 1)

    def test_completed_failure_is_propagated_without_sleeping(self):
        with patch('wait_ci.read_run', return_value=run(conclusion='failure')), patch('wait_ci.time.sleep') as sleep, patch('builtins.print'):
            self.assertEqual(wait_for_run(RUN_ID, SHA), 1)
            sleep.assert_not_called()

    def test_running_to_success_uses_one_metadata_read_per_interval(self):
        with patch('wait_ci.read_run', side_effect=[run('in_progress', None), run()]) as read, patch('wait_ci.time.sleep') as sleep, patch('builtins.print'):
            self.assertEqual(wait_for_run(RUN_ID, SHA, interval=120), 0)
            self.assertEqual(read.call_count, 2)
            sleep.assert_called_once_with(120)

    def test_timeout_is_not_reported_as_success(self):
        with patch('wait_ci.read_run', return_value=run('in_progress', None)), patch('wait_ci.time.monotonic', side_effect=[0, 100]), patch('wait_ci.time.sleep') as sleep, patch('builtins.print'):
            self.assertEqual(wait_for_run(RUN_ID, SHA, timeout=10), 124)
            sleep.assert_not_called()

    def test_invalid_arguments_do_not_request_github(self):
        for args in ((0, SHA, 60, 100), (123, 'short', 60, 100), (123, SHA, 1, 100), (123, SHA, 60, 0), (123, SHA, float('nan'), 100), (123, SHA, 60, float('inf'))):
            with self.subTest(args=args), patch('wait_ci.read_run') as read, self.assertRaises(ValueError):
                wait_for_run(*args)
            read.assert_not_called()
