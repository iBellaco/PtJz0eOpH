# Coach 1.1.10.591 (1307) · Alineación del autoescáner en teléfonos y tablets

Las cinco filas conservan su orden y separación aunque un aliado mueva su retrato durante la preselección. Coach usa el área capturada del juego cuando Android cambia su tamaño y mantiene la sesión de captura que el jugador autorizó.

## Cambios

- `DraftRingAlignment.kt` mide la altura con los aros visibles de varias filas. Cada aliado conserva su propio movimiento horizontal. Los puestos ocultos conservan sus posiciones previas.
- `DraftGeometryStability.kt` y `DraftVisionScanner.kt` comprueban las alturas y los diámetros juntos antes de publicar la cuadrícula de cada equipo. Dos recortes vecinos que se superponen se descartan. Una medición rival descartada permite que el siguiente fotograma aporte posiciones válidas.
- `ScreenCaptureManager.kt` redimensiona el mismo display virtual y sustituye la cola de imágenes. En Android 14 y posteriores usa el tamaño del contenido compartido. En las versiones anteriores usa los cambios de configuración de pantalla.
- La auditoría anterior dividía una lista por índices. Duplicaba el último escenario Samsung y omitía una resolución aun cuando el recuento global coincidía. `DraftMultiDeviceAuditTest.kt` y `tools/scanner-audit-summary.py` usan ahora la misma lista de modelos y dimensiones. El resumen verifica cada identificador, variante, recorte y resultado.
- `tools/scanner-sequence-plan.py`, `tools/scanner-sequence-summary.py` y los flujos de validación incluyen teléfonos y tablets Android. Cada configuración comprueba ambos órdenes de selección. El número de trabajos depende de las secuencias generadas.

## Comprobaciones

- En ocho capturas originales, los 75 puestos visibles quedaron dentro de seis píxeles de los centros anotados. El error máximo fue de 5,70 píxeles. La captura con la columna aliada tapada conserva sus cinco puestos como ocultos.
- La matriz de imágenes abarca nubia NX789J, nubia NX809J, POCO X7 Pro, Xiaomi Pad 6 y Galaxy Tab S6 Lite (2020). El original, el cambio de fondo y la permutación de campeones y baneos completaron 300 comprobaciones de identidad. El máximo error de centro en esta matriz fue de 3,15 píxeles de referencia. [Modelos y evidencia](device-compatibility.md)
- Las pruebas de bordes y márgenes ejercitan áreas de juego distintas dentro de estas pantallas. Las 5133 transformaciones adicionales comprueban únicamente las fórmulas de coordenadas.
- Pasaron 64 pruebas Android locales con JUnit y Robolectric, incluidas la sustitución de la superficie de captura y la estabilidad con retratos ocultos. Compiló el código de las pruebas instrumentadas. Pasaron 142 pruebas de las herramientas y los flujos de validación.
- El plan de OCR instalado contiene diez secuencias. La comprobación física de esta versión en las tablets y los teléfonos reportados sigue pendiente. [Contrato de reconocimiento](draft-recognition-contract.md)
