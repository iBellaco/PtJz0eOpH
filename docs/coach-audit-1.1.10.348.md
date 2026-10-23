# Coach 1.1.10.348 — build 1064

## Cambios

- Restablece Zaun como región y retira `premium_menu`, el estilo 13 de navegación. Las preferencias de ese estilo vuelven a la barra original.
- El consejo táctico ya no se reutiliza como sinergia. Los combos y relaciones con aliados se presentan en su sección; sin aliados confirmados se omite esa sección.
- Primer pick y alternativas comparten párrafos de ancho completo sin límite de líneas. Los botones de selección de alternativas tienen 48 dp.
- Las barras de referencia incorporan cápsula degradada, pestaña elevada, relieve inferior y borde curvo móvil con foco. Las etiquetas admiten letra ampliada.
- Amplía el filtro bilingüe de notas de balance: formas flexionadas, metadatos y cláusulas. Conserva temporizadores decimales, instrucciones útiles y recursos de jungla.
- Amplía la alineación de la captura S26 a tres resoluciones y genera un resumen que distingue imágenes simuladas de pruebas físicas.
- El inventario de recuperación recorre todas las páginas y conserva UNKNOWN ante respuestas incompletas. Un ensayo aislado comprueba atributos de cuenta, enlaces y bytes de adjuntos además del ensayo existente de documentos tipados.

## Archivos

- `AppThemeManager.kt`, `HextechOrbButton.kt`, `CoachNavStyleTest.kt`.
- `WildRiftRepository.kt`, `DraftAnalysisTab.kt`, `DraftAdviceSection.kt`.
- `ReferenceCoachNavBar.kt`, `VideoCoachNavBar.kt`, `ShowcaseScreen.kt`, traducciones portuguesas.
- `DraftRecommendationText.kt` y pruebas de texto, recomendaciones y lectura con letra ampliada.
- `DraftRingAlignmentTest.kt`, `tools/scanner-audit-summary.py`, workflow de validación.
- Inventario, pruebas de recuperación y `docs/recovery-runbook.md`.

## Verificación

- Siete pruebas del filtro Kotlin ejecutadas directamente con JUnit: correctas.
- Treinta y tres pruebas locales del servicio de cuentas: correctas, incluida paginación de inventario.
- La compilación Android, las pruebas visuales, los ensayos aislados, la firma y el APK instalado se validan en Actions antes de fusionar.

## Límites comprobables

- Esta sesión no tiene teléfonos conectados. Red Magic y Poco tienen confirmación previa del propietario; no se presentan como una prueba física nueva.
- La matriz automatizada usa 44 escenarios de captura (incluye diseños laterales sintéticos) y la captura S26 verifica exclusivamente la columna rival visible en tres resoluciones. No prueba una partida completa en S26.
- El ensayo de recuperación usa únicamente datos ficticios en emuladores locales. No demuestra recuperación productiva ni restauración de contraseñas.
- No se activó ningún servicio de pago. La cotización productiva requiere tamaño y región reales; se mantiene pendiente de revisión de costo.
