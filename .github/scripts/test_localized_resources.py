import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('localized_resources',
    Path(__file__).with_name('validate-localized-resources.py'))
validator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(validator)


class LocalizedResourcesTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        for locale in ('values', 'values-pt', 'values-pt-rBR'):
            (self.root / locale).mkdir()
            self.write(locale, ('app_name', 'overlay_app_settings', 'overlay_restricted_help'))

    def write(self, locale, keys):
        (self.root / locale / 'strings.xml').write_text('<resources>' + ''.join(
            f'<string name="{key}">Coach</string>' for key in keys) + '</resources>', encoding='utf-8')

    def test_complete_catalogs_pass(self):
        self.assertEqual([], validator.validate_resources(self.root))

    def test_exact_brazilian_regression_reports_both_missing_keys(self):
        self.write('values-pt-rBR', ('app_name',))
        self.assertEqual(['values-pt-rBR/strings.xml: faltan traducciones: '
                          'overlay_app_settings, overlay_restricted_help'],
                         validator.validate_resources(self.root))

    def test_generic_portuguese_does_not_hide_incomplete_brazilian_catalog(self):
        for locale in ('values-pt', 'values-pt-rBR'):
            self.write(locale, ('app_name',))
        issues = validator.validate_resources(self.root)
        self.assertEqual(2, len(issues))
        self.assertTrue(issues[0].startswith('values-pt/'))
        self.assertTrue(issues[1].startswith('values-pt-rBR/'))

    def test_duplicate_keys_fail(self):
        self.write('values-pt', ('app_name', 'app_name', 'overlay_app_settings', 'overlay_restricted_help'))
        self.assertIn('values-pt/strings.xml: clave duplicada: app_name', validator.validate_resources(self.root))

    def test_invalid_xml_fails(self):
        (self.root / 'values-pt-rBR/strings.xml').write_text('<resources>', encoding='utf-8')
        self.assertTrue(any('XML inaccesible o inválido' in issue for issue in validator.validate_resources(self.root)))

    def test_cli_returns_nonzero_for_missing_keys(self):
        self.write('values-pt-rBR', ('app_name',))
        errors = io.StringIO()
        with contextlib.redirect_stderr(errors):
            self.assertEqual(1, validator.main(['--resources', str(self.root)]))
        self.assertIn('overlay_restricted_help', errors.getvalue())


if __name__ == '__main__':
    unittest.main()
