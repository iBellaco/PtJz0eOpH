import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('obfuscation', Path(__file__).with_name('verify-release-obfuscation.py'))
obfuscation = importlib.util.module_from_spec(spec)
spec.loader.exec_module(obfuscation)


class ReleaseObfuscationTest(unittest.TestCase):
    def test_only_renamed_application_classes_count(self):
        mapping = ('com.example.util.Analysis -> com.example.wrdftx.o.a:\n'
                   'com.example.model.Champion -> com.example.model.Champion:\n'
                   'androidx.compose.Widget -> com.example.wrdftx.o.b:\n'
                   '    int value -> a\n')
        self.assertEqual(['com.example.wrdftx.o.a'], obfuscation.renamed_classes(mapping))


if __name__ == '__main__':
    unittest.main()
