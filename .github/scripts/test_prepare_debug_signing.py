import base64
import hashlib
import importlib.util
from pathlib import Path
import tempfile
import types
import unittest

spec = importlib.util.spec_from_file_location('signing', Path(__file__).with_name('prepare-debug-signing.py'))
signing = importlib.util.module_from_spec(spec)
spec.loader.exec_module(signing)


class SigningPolicyTest(unittest.TestCase):
    def invoke(self, environment):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            calls = []

            def run(command, **options):
                calls.append(command[1])
                if command[1] == '-genkeypair':
                    (root / 'debug.keystore').write_bytes(b'test-only-key')
                return types.SimpleNamespace(stdout=b'test-certificate')

            fingerprint = signing.prepare(root, root / 'android', environment, run)
            self.assertEqual(0o600, (root / 'github.keystore').stat().st_mode & 0o777)
            self.assertEqual((root / 'debug.keystore').read_bytes(), (root / 'android/debug.keystore').read_bytes())
            return fingerprint, calls

    def test_publication_without_identity_never_generates_a_replacement_key(self):
        with self.assertRaisesRegex(RuntimeError, 'Publicación bloqueada'):
            self.invoke({'COACH_REQUIRE_PERSISTENT_SIGNING': 'true'})

    def test_wrong_certificate_cannot_publish(self):
        with self.assertRaisesRegex(RuntimeError, 'no coincide'):
            self.invoke({'COACH_REQUIRE_PERSISTENT_SIGNING': 'true',
                         'COACH_DEBUG_KEYSTORE_BASE64': base64.b64encode(b'test-only-key').decode(),
                         'COACH_SIGNING_CERT_SHA256': '0' * 64})

    def test_valid_identity_is_reused_without_generating_a_new_key(self):
        expected = hashlib.sha256(b'test-certificate').hexdigest()
        actual, calls = self.invoke({'COACH_REQUIRE_PERSISTENT_SIGNING': 'true',
                                   'COACH_DEBUG_KEYSTORE_BASE64': base64.b64encode(b'test-only-key').decode(),
                                   'COACH_SIGNING_CERT_SHA256': expected})
        self.assertEqual(expected, actual)
        self.assertNotIn('-genkeypair', calls)

    def test_test_builds_can_use_an_ephemeral_key(self):
        _, calls = self.invoke({})
        self.assertIn('-genkeypair', calls)

    def test_invalid_key_fails_without_logging_its_contents(self):
        with self.assertRaisesRegex(RuntimeError, 'base64 válido'):
            self.invoke({'COACH_DEBUG_KEYSTORE_BASE64': 'invalid-secret-content!'})


if __name__ == '__main__':
    unittest.main()
