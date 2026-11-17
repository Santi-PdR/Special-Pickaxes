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
                    self.assertIn('%1$s', text)
                    self.assertLess(len(text), 240)
                    for retired in ('outline', 'contorno', 'visible border', 'borde visible', 'flash', 'destello'):
                        self.assertNotIn(retired, text.lower())

    def test_key_dimensions_and_functional_summaries_remain_visible(self):
        for locale in ('en_us', 'es_es'):
            texts = self.texts(locale)
            for identity, key, facts in (
                ('hellspec', 'main', ('25×7×25', '1679')),
                ('hellspec', 'alt', ('11×11', '48')),
                ('worldloom', 'alt', ('64',)),
                ('palimpsest', 'main', ('1024',)),
                ('palimpsest', 'alt', ('32',)),
            ):
                text = texts[f'guide.{key}.{identity}']
                for fact in facts:
                    self.assertIn(fact, text)
                self.assertLess(len(text), 200)
