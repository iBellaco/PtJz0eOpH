import importlib.util
import json
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('private_signing', Path(__file__).with_name('restore-private-signing.py'))
private = importlib.util.module_from_spec(spec)
spec.loader.exec_module(private)


class PrivateSigningTest(unittest.TestCase):
    manifest = {'repository': 'example/coach', 'release_id': 123,
                'certificate_sha256': 'a' * 64}

    def document(self, **changes):
        return {'id': 123, 'draft': True,
                'body': json.dumps({'schema': 1, 'certificate_sha256': 'a' * 64,
                                    'keystore_base64': 'test-only-value'}), **changes}

    def test_private_identity_can_be_restored_without_secret_api_permissions(self):
        result = private.resolve_environment({}, self.manifest, lambda _: self.document())
        self.assertEqual('true', result['COACH_REQUIRE_PERSISTENT_SIGNING'])
        self.assertEqual('a' * 64, result['COACH_SIGNING_CERT_SHA256'])

    def test_public_or_different_release_is_never_used(self):
        for changes in [{'draft': False}, {'draft': None}, {'id': 124}]:
            with self.assertRaises(RuntimeError):
                private.private_environment(self.document(**changes), self.manifest)

    def test_changed_fingerprint_and_invalid_body_are_rejected(self):
        for body in ['not-json', json.dumps({'schema': 1, 'certificate_sha256': 'b' * 64}),
                     json.dumps({'schema': 1, 'certificate_sha256': 'a' * 64,
                                 'keystore_base64': ''})]:
            with self.assertRaises(RuntimeError):
                private.private_environment(self.document(body=body), self.manifest)

    def test_configured_secret_takes_precedence(self):
        def forbidden(_):
            self.fail('A configured secret must not read the draft')
        result = private.resolve_environment({'COACH_DEBUG_KEYSTORE_BASE64': 'test-only-value',
                                              'COACH_SIGNING_CERT_SHA256': 'b' * 64},
                                             self.manifest, forbidden)
        self.assertEqual('b' * 64, result['COACH_SIGNING_CERT_SHA256'])

    def test_publication_never_falls_back_to_a_temporary_key(self):
        def denied(_):
            raise RuntimeError('private access denied')
        with self.assertRaises(RuntimeError):
            private.resolve_environment({'COACH_REQUIRE_PERSISTENT_SIGNING': 'true',
                                         'COACH_ALLOW_EPHEMERAL_TEST_SIGNING': 'true'},
                                        self.manifest, denied)

    def test_fork_tests_do_not_request_the_private_key(self):
        def forbidden(_):
            self.fail('Fork tests must not read the draft')
        result = private.resolve_environment({'COACH_ALLOW_EPHEMERAL_TEST_SIGNING': 'true'},
                                             self.manifest, forbidden)
        self.assertEqual({'COACH_REQUIRE_PERSISTENT_SIGNING': 'false'}, result)


if __name__ == '__main__':
    unittest.main()
