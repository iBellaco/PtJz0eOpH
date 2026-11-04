"""Test the actual audit functions without starting an Android device."""
import ast
from pathlib import Path
import unittest
from unittest.mock import Mock
import xml.etree.ElementTree as ET

source = ast.parse(Path(__file__).with_name('audit-portuguese-device.py').read_text())
functions = ast.Module(body=[n for n in source.body if isinstance(n, ast.FunctionDef) and n.name in
    ('app_nodes', 'is_channel_name', 'is_announcement_body', 'ui_text_candidates', 'node_bounds',
     'onboarding_fallback_point', 'finish_onboarding')], type_ignores=[])
namespace = {'ET': ET, 'APP': 'com.Coach', 're': __import__('re')}
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

    def test_remote_announcement_body_is_authored_but_dialog_labels_are_checked(self):
        nodes = [
            {'text': 'AVISO URGENTE / MANUTENÇÃO'},
            {'text': 'Entendido'},
            {'class': 'android.widget.ScrollView', 'text': 'mensaje en español'},
            {'class': 'android.widget.TextView', 'text': 'etiqueta en español'},
        ]
        self.assertEqual(['AVISO URGENTE / MANUTENÇÃO', 'Entendido', 'etiqueta en español'], candidates(nodes))
        self.assertEqual(['mensaje en español', 'etiqueta en español'], candidates(nodes[2:]))

    def test_only_application_nodes_enter_the_audit(self):
        xml = '<hierarchy><node package="com.Coach" text="Início"/><node package="android" text="Español"/></hierarchy>'
        self.assertEqual(['Início'], candidates(namespace['app_nodes'](xml)))

    def test_onboarding_fallback_prefers_wide_clickable_control_at_bottom(self):
        nodes = [
            {'clickable': 'true', 'enabled': 'true', 'bounds': '[20,40][120,90]'},
            {'clickable': 'true', 'enabled': 'true', 'bounds': '[24,540][296,592]'},
            {'clickable': 'false', 'enabled': 'true', 'bounds': '[0,600][320,640]'},
        ]
        self.assertEqual((160, 566), namespace['onboarding_fallback_point'](nodes, 320, 640))

    def test_onboarding_fallback_retains_safe_coordinate_for_incomplete_tree(self):
        self.assertEqual((160, 576), namespace['onboarding_fallback_point']([], 320, 640))

    def test_last_onboarding_page_requires_dashboard_and_retries(self):
        state = dict(namespace)
        try:
            namespace['tap'] = Mock()
            namespace['window'] = Mock(return_value='screen')
            namespace['app_nodes'] = Mock(side_effect=[
                [{'text': '¡Comenzar ahora!'}] for _ in range(12)
            ] + [[{'text': '🎨'}]])
            namespace['time'] = Mock()
            namespace['finish_onboarding']('¡Comenzar ahora!')
            self.assertEqual(2, namespace['tap'].call_count)
        finally:
            namespace.clear()
            namespace.update(state)
