import subprocess
import contextlib
import io
import sys
import unittest
from unittest.mock import Mock
from gradle_dependency_retry import run, execute, transient_resolution_failure, MAX_DIAGNOSTIC_CHARS

TIMEOUT = "A problem occurred configuring root project.\nCould not resolve files for ':_compileJava_1'.\nRead timed out\n"


class DependencyRetryTests(unittest.TestCase):
    def test_retries_one_pre_task_timeout_then_returns_success(self):
        runner = Mock(side_effect=[subprocess.CompletedProcess([], 1, TIMEOUT),
                                   subprocess.CompletedProcess([], 0, 'BUILD SUCCESSFUL\n')])
        sleep = Mock()
        self.assertEqual(0, run(['./gradlew', 'build', 'test'], runner, sleep))
        self.assertEqual(2, runner.call_count)
        sleep.assert_called_once_with(60)
        self.assertEqual(['./gradlew', 'build', 'test'], runner.call_args.args[0])

    def test_persistent_timeout_stays_failure_after_exactly_two_attempts(self):
        runner = Mock(return_value=subprocess.CompletedProcess([], 7, TIMEOUT))
        sleep = Mock()
        self.assertEqual(7, run(['./gradlew'], runner, sleep))
        self.assertEqual(2, runner.call_count)
        sleep.assert_called_once_with(60)

    def test_task_compiler_tests_and_missing_artifacts_never_retry(self):
        for output in (TIMEOUT + '> Task :compileJava FAILED\n',
                       TIMEOUT + '> Task :test FAILED\n',
                       TIMEOUT + 'Could not find required.jar\n',
                       'compiler error: cannot find symbol\n',
                       TIMEOUT + 'SSLHandshakeException\n'):
            runner = Mock(return_value=subprocess.CompletedProcess([], 1, output))
            sleep = Mock()
            self.assertEqual(1, run(['./gradlew'], runner, sleep))
            self.assertEqual(1, runner.call_count)
            sleep.assert_not_called()

    def test_only_configuration_phase_429_is_transient(self):
        self.assertTrue(transient_resolution_failure(TIMEOUT.replace('Read timed out', '429 from server')))
        self.assertFalse(transient_resolution_failure('429 Too Many Requests'))
        self.assertFalse(transient_resolution_failure(TIMEOUT + 'BUILD SUCCESSFUL'))

    def test_signals_and_empty_commands_cannot_report_success(self):
        runner = Mock(return_value=subprocess.CompletedProcess([], -15, 'interrupted'))
        self.assertEqual(1, run(['./gradlew'], runner, Mock()))
        with self.assertRaises(ValueError):
            run([], runner, Mock())

    def test_real_executor_streams_output_and_keeps_the_real_exit_status(self):
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            result = execute([sys.executable, '-c', "print('live output'); raise SystemExit(9)"])
        self.assertEqual(9, result.returncode)
        self.assertEqual('live output\n', output.getvalue())
        self.assertTrue(result.streamed)
        self.assertEqual('live output\n', result.stdout)

    def test_truncated_diagnostics_are_bounded_and_cannot_authorize_retry(self):
        output = io.StringIO()
        script = "print('x' * " + str(MAX_DIAGNOSTIC_CHARS + 1000) + "); print(" + repr(TIMEOUT) + "); raise SystemExit(1)"
        with contextlib.redirect_stdout(output):
            result = execute([sys.executable, '-c', script])
        self.assertGreater(len(output.getvalue()), MAX_DIAGNOSTIC_CHARS)
        self.assertLess(len(result.stdout), MAX_DIAGNOSTIC_CHARS + 100)
        self.assertFalse(transient_resolution_failure(result.stdout))

    def test_streamed_logs_are_not_printed_twice(self):
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            code = run([sys.executable, '-c', "print('one unique log line')"])
        self.assertEqual(0, code)
        self.assertEqual(1, output.getvalue().count('one unique log line'))
