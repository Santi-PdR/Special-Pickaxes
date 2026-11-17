import unittest
from client_input_gate import ClientInputGate


class ClientInputGateTests(unittest.TestCase):
    def test_requires_actual_manual_render_then_close_before_activation(self):
        gate = ClientInputGate()
        gate.begin('paradox_crucible-andesite')
        self.assertIsNone(gate.observe('MANUAL_SCREEN_CLOSED'))
        self.assertEqual(gate.observe('MANUAL_SCREEN_RENDERED 640x360'), 'capture_and_close')
        self.assertIsNone(gate.observe('MANUAL_SCREEN_RENDERED 640x360'))
        self.assertEqual(gate.observe('MANUAL_SCREEN_CLOSED'), 'activate')
        self.assertIsNone(gate.observe('MANUAL_SCREEN_CLOSED'))
        gate.complete('paradox_crucible-andesite')
        self.assertIsNone(gate.stage)

    def test_confirm_is_only_for_regional_fixture(self):
        gate = ClientInputGate()
        gate.begin('paradox_crucible-regional')
        gate.observe('MANUAL_SCREEN_RENDERED 640x360')
        self.assertEqual(gate.observe('MANUAL_SCREEN_CLOSED'), 'confirm')

    def test_missing_close_never_authorizes_activation(self):
        gate = ClientInputGate()
        gate.begin('hellspec-hellforge')
        gate.observe('MANUAL_SCREEN_RENDERED 640x360')
        for line in ('', 'TOOLTIP_VALIDATED', 'CLIENT_INPUT_EDGE_ACTIVATE', 'MANUAL_SCREEN_RENDERED 640x360'):
            self.assertIsNone(gate.observe(line))
        with self.assertRaises(RuntimeError):
            gate.complete('hellspec-hellforge')

    def test_wrong_or_duplicate_scene_cannot_complete_or_replace_pending_input(self):
        gate = ClientInputGate()
        gate.begin('worldloom-tunnel')
        with self.assertRaises(RuntimeError):
            gate.begin('palimpsest-survey')
        gate.observe('MANUAL_SCREEN_RENDERED 640x360')
        gate.observe('MANUAL_SCREEN_CLOSED')
        with self.assertRaises(RuntimeError):
            gate.complete('wrong-scene')
        gate.complete('worldloom-tunnel')
        with self.assertRaises(RuntimeError):
            gate.complete('worldloom-tunnel')
