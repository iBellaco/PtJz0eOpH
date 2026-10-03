import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('apk_signing', Path(__file__).with_name('verify-apk-signing.py'))
apk = importlib.util.module_from_spec(spec)
spec.loader.exec_module(apk)


class ApkCertificateTest(unittest.TestCase):
    expected = 'a' * 64

    def test_old_verifier_format(self):
        self.assertTrue(apk.certificate_matches('Number of signers: 1\nSigner #1 certificate SHA-256 digest: ' + self.expected, self.expected))

    def test_android_37_format(self):
        self.assertTrue(apk.certificate_matches('Number of signers: 1\nV3.0 Signer: certificate SHA-256 digest: ' + self.expected, self.expected))

    def test_same_certificate_across_verified_schemes(self):
        self.assertTrue(apk.certificate_matches('Number of signers: 1\nV3.0 Signer: certificate SHA-256 digest: ' + self.expected + '\nV3.1 Signer: certificate SHA-256 digest: ' + self.expected.upper(), self.expected))

    def test_wrong_certificate_rejected(self):
        self.assertFalse(apk.certificate_matches('Number of signers: 1\nV3.0 Signer: certificate SHA-256 digest: ' + 'b' * 64, self.expected))

    def test_key_digest_does_not_count_as_certificate(self):
        self.assertFalse(apk.certificate_matches('Number of signers: 1\nV3.0 Signer: public key SHA-256 digest: ' + self.expected, self.expected))

    def test_missing_or_multiple_signers_rejected(self):
        self.assertFalse(apk.certificate_matches('', self.expected))
        self.assertFalse(apk.certificate_matches('Number of signers: 2\nSigner #1 certificate SHA-256 digest: ' + self.expected, self.expected))

    def test_unchanged_validated_apk_matches_merged_source(self):
        current = {'source_tree': 'same', 'apk_sha256': 'hash', 'certificate_sha256': 'cert',
                   'version_name': '1.1.10.164', 'version_code': 880}
        self.assertTrue(apk.provenance_matches({'schema': 1, **current}, current))

    def test_changed_source_binary_certificate_or_version_prevents_publication(self):
        current = {'source_tree': 'same', 'apk_sha256': 'hash', 'certificate_sha256': 'cert',
                   'version_name': '1.1.10.164', 'version_code': 880}
        for field in current:
            self.assertFalse(apk.provenance_matches({'schema': 1, **current, field: 'different'}, current))


if __name__ == '__main__':
    unittest.main()
