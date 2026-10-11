import subprocess
import tempfile
import unittest
from pathlib import Path

class OptionalFunctionDeploymentTest(unittest.TestCase):
    def allowed(self, log):
        with tempfile.NamedTemporaryFile(mode='w') as file:
            file.write(log)
            file.flush()
            return subprocess.run(['bash', str(Path(__file__).with_name('can-defer-functions.sh')), file.name], check=False).returncode == 0

    def test_missing_cloud_build_permission_preserves_the_verified_worker_path(self):
        self.assertTrue(self.allowed('Error: Cloud Functions deployment requires the Cloud Build API to be enabled. The current credentials do not have permission to enable APIs for project example.'))
        self.assertTrue(self.allowed('HTTP Error: 403, Permission denied to get service'))

    def test_application_and_unclassified_deployment_errors_are_not_deferred(self):
        for log in ['SyntaxError: invalid function', 'Error: invalid authentication credentials', 'Cloud Functions deployment requires the Cloud Build API to be enabled.', 'HTTP Error: 500', '']:
            self.assertFalse(self.allowed(log), log)

if __name__ == '__main__':
    unittest.main()
