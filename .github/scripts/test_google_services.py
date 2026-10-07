import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('google_config', Path(__file__).with_name('restore-google-services.py'))
config = importlib.util.module_from_spec(spec)
spec.loader.exec_module(config)


class GoogleServicesTests(unittest.TestCase):
    def test_missing_secret_cannot_fall_back_to_public_values(self):
        with tempfile.TemporaryDirectory() as root:
            with self.assertRaisesRegex(ValueError, 'Falta el Secret'):
                config.restore(Path(root), {})
            self.assertFalse((Path(root) / 'app/google-services.json').exists())

    def test_malformed_secret_is_not_exposed_in_error(self):
        with tempfile.TemporaryDirectory() as root:
            for value in ['PRIVATE_MARKER', '{"client": "PRIVATE_MARKER"}', 'null', '[]']:
                with self.assertRaises(ValueError) as failure:
                    config.restore(Path(root), {'COACH_GOOGLE_SERVICES_JSON': value})
                self.assertNotIn('PRIVATE_MARKER', str(failure.exception))

    def test_wrong_android_package_is_rejected(self):
        document = self.document('other.app')
        with tempfile.TemporaryDirectory() as root:
            with self.assertRaisesRegex(ValueError, 'com.Coach'):
                config.restore(Path(root), {'COACH_GOOGLE_SERVICES_JSON': json.dumps(document)})

    def test_valid_secret_restores_private_local_file(self):
        document = self.document('com.Coach')
        with tempfile.TemporaryDirectory() as root:
            target = config.restore(Path(root), {'COACH_GOOGLE_SERVICES_JSON': json.dumps(document)})
            self.assertEqual(document, json.loads(target.read_text()))
            self.assertEqual(0o600, target.stat().st_mode & 0o777)

    @staticmethod
    def document(package):
        return {'project_info': {'project_id': 'example-project'}, 'client': [
            {'client_info': {'android_client_info': {'package_name': package}},
             'api_key': [{'current_key': 'test-value'}]}]}
