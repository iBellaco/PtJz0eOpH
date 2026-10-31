"""Test the actual audit functions without starting an Android device."""
import ast
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

source = ast.parse(Path(__file__).with_name('audit-portuguese-device.py').read_text())
functions = ast.Module(body=[n for n in source.body if isinstance(n, ast.FunctionDef) and n.name in
    ('app_nodes', 'is_channel_name', 'ui_text_candidates')], type_ignores=[])
namespace = {'ET': ET, 'APP': 'com.Coach'}
exec(compile(functions, 'audit-portuguese-device.py', 'exec'), namespace)
candidates = namespace['ui_text_candidates']


class DeviceTextAuditTest(unittest.TestCase):
    def test_channel_brand_is_preserved_and_its_live_label_still_checked(self):
        self.assertEqual(['Ao vivo'], candidates([
            {'resource-id': 'streamer_channel_name', 'text': 'hola'},
            {'resource-id': 'streamer_live_label', 'text': 'Ao vivo'}]))

    def test_identical_spanish_word_elsewhere_is_still_checked(self):
        self.assertEqual(['hola'], candidates([
            {'resource-id': 'com.Coach:id/streamer_channel_name', 'text': 'hola'},
            {'resource-id': 'other_label', 'text': 'hola'}]))

    def test_an_untagged_or_similarly_named_label_is_not_exempt(self):
        self.assertEqual(['hola', 'en vivo'], candidates([
            {'text': 'hola'}, {'resource-id': 'streamer_channel_name_other', 'text': 'en vivo'}]))

    def test_accessibility_text_is_always_checked(self):
        self.assertEqual(['en vivo'], candidates([
            {'resource-id': 'streamer_channel_name', 'text': 'hola', 'content-desc': 'en vivo'}]))

    def test_only_application_nodes_enter_the_audit(self):
        xml = '<hierarchy><node package="com.Coach" text="Início"/><node package="android" text="Español"/></hierarchy>'
        self.assertEqual(['Início'], candidates(namespace['app_nodes'](xml)))
