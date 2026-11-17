import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch
import run_manual_models as models


class ManualModelsRunnerTests(unittest.TestCase):
    def test_failed_compile_removes_stale_success_and_never_runs_java(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            report = root / 'build/manual-models/report.json'
            report.parent.mkdir(parents=True)
            report.write_text('{"checks":99999}')
            with patch.object(models, 'ROOT', root), patch.object(models.subprocess, 'run', side_effect=subprocess.CalledProcessError(1, 'javac')) as run:
                with self.assertRaises(subprocess.CalledProcessError):
                    models.main()
                self.assertFalse(report.exists())
                self.assertEqual(1, run.call_count)
                self.assertEqual('javac', run.call_args.args[0][0])

    def test_success_is_bound_to_source_hashes_and_explicit_model_scope(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / 'Model.java').write_text('class Model {}')
            result = subprocess.CompletedProcess([], 0, 'MANUAL_MODELS_OK checks=15000 scope=pure_java_only\n', '')
            with patch.object(models, 'ROOT', root), patch.object(models, 'SOURCES', ['Model.java']), patch.dict(os.environ, {'GITHUB_SHA': 'a' * 40}), patch.object(models.subprocess, 'run', return_value=result):
                models.main()
            report = json.loads((root / 'build/manual-models/report.json').read_text())
            self.assertEqual(15000, report['checks'])
            self.assertEqual('a' * 40, report['commit'])
            self.assertIn('not rendering', report['scope'])
            self.assertEqual(hashlib.sha256(b'class Model {}').hexdigest(), report['sources']['Model.java'])

    def test_missing_or_incomplete_completion_never_writes_success(self):
        for text in ('', 'MANUAL_MODELS_OK checks=1 scope=pure_java_only\n'):
            with tempfile.TemporaryDirectory() as folder:
                root = Path(folder)
                result = subprocess.CompletedProcess([], 0, text, '')
                with patch.object(models, 'ROOT', root), patch.object(models.subprocess, 'run', return_value=result):
                    with self.assertRaises(RuntimeError):
                        models.main()
                self.assertFalse((root / 'build/manual-models/report.json').exists())
