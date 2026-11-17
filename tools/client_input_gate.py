"""Fail-closed graphical scene input ordering; no retries or synthetic activation success."""
class ClientInputGate:
    def __init__(self):
        self.scene = None
        self.stage = None

    def begin(self, scene):
        if self.stage is not None:
            raise RuntimeError('Previous scene input is still pending')
        self.scene = scene
        self.stage = 'manual_render'

    def observe(self, line):
        if self.stage == 'manual_render' and 'MANUAL_SCREEN_RENDERED ' in line:
            self.stage = 'manual_close'
            return 'capture_and_close'
        if self.stage == 'manual_close' and 'MANUAL_SCREEN_CLOSED' in line:
            self.stage = 'activation'
            return 'confirm' if self.scene == 'paradox_crucible-regional' else 'activate'
        return None

    def complete(self, scene):
        if self.stage != 'activation' or scene != self.scene:
            raise RuntimeError('Scene activation arrived out of order: ' + scene)
        self.stage = None
        self.scene = None
