import unittest
from evidence_checks import select_export


def chunk(index, total=2, *, identity=None, digest='a', attempt=None, run='123'):
    return {'id': identity or index, 'name': f'Build evidence {index:02}/{total:02}',
            'output': {'summary': f'Run {run}. ' + (f'Attempt {attempt}. ' if attempt else '')
                       + f'ZIP base64 chunk {index}/{total}. SHA256(zip): {digest * 64}'}}


class EvidenceChecksTests(unittest.TestCase):
    def test_old_format_remains_readable_and_chunks_are_sorted(self):
        checks, digest = select_export([chunk(2), chunk(1)], '123')
        self.assertEqual([check['id'] for check in checks], [1, 2])
        self.assertEqual(digest, 'a' * 64)

    def test_latest_attempt_never_mixes_with_old_chunks(self):
        checks = [chunk(1), chunk(2), chunk(1, identity=11, digest='b', attempt=2),
                  chunk(2, identity=12, digest='b', attempt=2)]
        selected, digest = select_export(checks, '123')
        self.assertEqual([check['id'] for check in selected], [11, 12])
        self.assertEqual(digest, 'b' * 64)

    def test_incomplete_new_attempt_does_not_fall_back_to_stale_evidence(self):
        with self.assertRaisesRegex(ValueError, 'Incomplete'):
            select_export([chunk(1), chunk(2), chunk(1, identity=11, digest='b', attempt=2)], '123')

    def test_legacy_rerun_without_attempt_metadata_uses_newest_digest(self):
        selected, digest = select_export([chunk(1), chunk(2), chunk(1, identity=11, digest='b'),
                                         chunk(2, identity=12, digest='b')], '123')
        self.assertEqual(digest, 'b' * 64)
        self.assertEqual(len(selected), 2)

    def test_duplicate_identical_chunks_use_the_latest_check(self):
        selected, _ = select_export([chunk(1), chunk(2), chunk(1, identity=20)], '123')
        self.assertEqual([check['id'] for check in selected], [20, 2])

    def test_malformed_indices_or_totals_are_rejected(self):
        for checks in ([chunk(0)], [chunk(3)], [chunk(1, 0)], [chunk(1, 999999999)], [chunk(1, 1), chunk(2, 2)]):
            with self.subTest(checks=checks), self.assertRaises(ValueError):
                select_export(checks, '123')

    def test_other_runs_and_unrelated_checks_cannot_supply_parts(self):
        with self.assertRaises(ValueError):
            select_export([chunk(1), chunk(2, run='456'), {'id': 100, 'name': 'forge', 'output': {}}], '123')
        with self.assertRaisesRegex(ValueError, 'No evidence'):
            select_export([chunk(1, run='456')], '123')
