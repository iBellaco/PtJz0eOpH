import importlib.util
import io
import os
from pathlib import Path
import tempfile
import unittest
from contextlib import redirect_stdout
from unittest.mock import patch
import xml.etree.ElementTree as ET

spec = importlib.util.spec_from_file_location('junit', Path(__file__).with_name('run-jvm-regression.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class JunitFailureReportTest(unittest.TestCase):
    def test_cases_are_visible_without_exposing_emails_urls_or_tokens(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary) / 'app/build/test-results/testDebugUnitTest'
            directory.mkdir(parents=True)
            root = ET.Element('testsuite')
            case = ET.SubElement(root, 'testcase', name='recoverOffline')
            ET.SubElement(case, 'failure', message='10%\nuser@example.invalid token=private-value https://private.invalid/trace')
            ET.ElementTree(root).write(directory / 'TEST-com.example.DirectoryTest.xml', encoding='utf-8')
            output = io.StringIO()
            with patch.object(module, 'ROOT', Path(temporary)), patch.dict(os.environ, {'GITHUB_ACTIONS': 'true'}), redirect_stdout(output):
                module.report_failures()
            value = output.getvalue()
            self.assertIn('com.example.DirectoryTest.recoverOffline', value)
            self.assertIn('::error title=', value)
            self.assertIn('10%25%0A', value)
            for private in ['user@example.invalid', 'private-value', 'https://private.invalid']:
                self.assertNotIn(private, value)
