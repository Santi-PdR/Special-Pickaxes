"""Localized presentation contracts; actual rendering still requires graphical CI."""
import json
from pathlib import Path
import unittest
from mining_catalog import IDS

LANG = Path('src/main/resources/assets/specialpickaxes/lang')


class InterfaceCopyTests(unittest.TestCase):
    def texts(self, locale):
        return json.loads((LANG / (locale + '.json')).read_text())

    def test_playable_names_are_short_unique_and_do_not_change_registry_ids(self):
        for locale in ('en_us', 'es_es'):
            texts = self.texts(locale)
            names = [texts['item.specialpickaxes.' + identity] for identity in IDS]
            self.assertEqual(len(names), 13)
            self.assertEqual(len(set(names)), 13)
            for name in names:
                self.assertTrue(3 <= len(name) <= 26, name)
            for identity in IDS:
                model = Path('src/main/resources/assets/specialpickaxes/models/item') / (identity + '.json')
                self.assertTrue(model.is_file())

    def test_footer_uses_a_live_key_placeholder_in_both_languages(self):
        for locale in ('en_us', 'es_es'):
            footer = self.texts(locale)['screen.specialpickaxes.close']
            self.assertEqual(footer.count('%s'), 1)
            self.assertNotIn('I ', footer)
        source = Path('src/main/java/io/github/santipdr/specialpickaxes/client/ArtifactManualScreen.java').read_text()
        self.assertIn('"screen.specialpickaxes.close",RelicKeys.MANUAL.getTranslatedKeyMessage()', source)
        self.assertIn('fit(selected,footerLeftWidth)', source)
        self.assertIn('fit(modeLabel,modeWidth)', source)

    def test_active_shift_instructions_are_concise_and_do_not_promise_retired_cosmetics(self):
        for locale in ('en_us', 'es_es'):
            for key, text in self.texts(locale).items():
                if not key.startswith('mining.how.') or key.split('.')[2] not in IDS:
                    continue
                with self.subTest(locale=locale, key=key):
                    self.assertTrue(text.startswith('%1$s · '), text)
                    self.assertLess(len(text), 240)
                    for retired in ('outline', 'contorno', 'visible border', 'borde visible', 'flash', 'destello'):
                        self.assertNotIn(retired, text.lower())

    def test_key_dimensions_and_functional_summaries_remain_visible(self):
        for locale in ('en_us', 'es_es'):
            texts = self.texts(locale)
            for identity in ('hellspec','worldloom','palimpsest','eventide','paradox_crucible'):
                for action in ('main','alt'):
                    text=texts[f'guide.{action}.{identity}']
                    self.assertTrue(15<len(text)<200)
                    for retired in ('4049','3571','2765','7153','4164'):
                        self.assertNotIn(retired,text)

    def test_manual_renders_the_equipped_details_for_all_thirteen_in_three_languages(self):
        source = Path('src/main/java/io/github/santipdr/specialpickaxes/client/ArtifactManualScreen.java').read_text()
        self.assertIn('Component.translatable("curios.detail.specialpickaxes."+kind.id,RelicKeys.name(RelicControl.Action.CURIO_ACTIVATE))', source)
        for locale in ('en_us', 'es_es', 'es_uy'):
            texts = self.texts(locale)
            for identity in IDS:
                self.assertTrue(texts['curios.detail.specialpickaxes.' + identity].strip())
            for identity in IDS:
                detail = texts['curios.detail.specialpickaxes.' + identity]
                self.assertLessEqual(len(detail), 200, (locale, identity))
                self.assertIn('%s:', detail)

    def test_every_active_mode_has_readable_localized_labels(self):
        from mining_catalog import MODES
        modes = {mode for identity in IDS for mode in MODES[identity]}
        for locale in ('en_us', 'es_es'):
            texts = self.texts(locale)
            for mode in modes:
                value = texts['mode.specialpickaxes.' + mode]
                self.assertTrue(3 <= len(value) <= 26, (locale, mode, value))
                self.assertNotEqual(value, value.upper(), (locale, mode))
                if locale == 'es_es':
                    self.assertNotEqual(value.lower(), mode.replace('_', ' '), mode)

    def test_manual_supports_small_keyboard_scroll_steps_and_shows_navigation(self):
        source = Path('src/main/java/io/github/santipdr/specialpickaxes/client/ArtifactManualScreen.java').read_text()
        for key in ('GLFW_KEY_UP', 'GLFW_KEY_DOWN', 'GLFW_KEY_PAGE_UP', 'GLFW_KEY_PAGE_DOWN'):
            self.assertIn(key, source)
        for locale in ('en_us', 'es_es'):
            self.assertIn('↑↓', self.texts(locale)['screen.specialpickaxes.navigation'])

    def test_browsing_copy_picker_never_sends_copy_intents(self):
        source = Path('src/main/java/io/github/santipdr/specialpickaxes/client/ArtifactManualScreen.java').read_text()
        browse = source.split('if(keyCode==org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT', 1)[1].split('if(choiceFocus>=0&&(keyCode==org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER', 1)[0]
        self.assertIn('GLFW_KEY_TAB', browse)
        self.assertIn('hasShiftDown()', browse)
        self.assertNotIn('chooseCopy', browse)
        self.assertIn('if(!confirmingCopy)', source)

    def test_copy_preview_and_help_are_localized_and_cache_tracks_picker_availability(self):
        for locale in ('en_us', 'es_es'):
            texts = self.texts(locale)
            self.assertEqual(texts['screen.specialpickaxes.copy_preview'].count('%s'), 1)
            self.assertIn('Tab', texts['guide.control.copy_picker'])
            self.assertIn('Enter', texts['guide.control.copy_picker'])
        source = Path('src/main/java/io/github/santipdr/specialpickaxes/client/ArtifactManualScreen.java').read_text()
        self.assertIn('if(selectingCopy())controls.add', source)
        self.assertIn('.append(selectingCopy())', source)
        self.assertIn('String copied=selectedCopyId(active)', source)

    def test_mode_hot_paths_do_not_allocate_mutable_arrays(self):
        source = Path('src/main/java/io/github/santipdr/specialpickaxes/artifact/ArtifactInteraction.java').read_text()
        hot = source.split('public static int modeCount', 1)[1].split('private static void resetDisplay', 1)[0]
        self.assertNotIn('modes(k)', hot)
        self.assertNotIn('new String', hot)
        self.assertIn('Math.floorMod', hot)
