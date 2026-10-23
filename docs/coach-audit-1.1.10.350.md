# Coach 1.1.10.350 — build 1066

## Alcance

La entrega anterior ya fue fusionada por el propietario; las tres ejecuciones
recientes de main terminaron correctamente. Su auditoría de 54 perfiles y 162
variantes comprobaba geometría y retratos, no la secuencia OCR completa 1–9.
Esta intervención añade esa comprobación con el lector nativo del APK instalado.

Se conserva la captura del propietario (1280 × 579, SHA-256
42661aefae3df52b4f72047c039120f72f4ea351139d7bdb5107351fe8d3342a).
La matriz de retratos y baneos incorpora nueve perfiles documentados de Xiaomi,
Redmi y POCO: 63 perfiles, 41 dimensiones distintas y 189 variantes.
Las referencias físicas Red Magic y Poco no cambian.

La revisión encontró que las bandas OCR mantenían el alto físico de la captura
cuando el juego estaba ajustado con bandas exteriores. Ahora siguen el alto del
viewport observado, al igual que los retratos, evitando invadir otra fila.
El atlas amplía proporcionalmente las bandas pequeñas (entre 2× y 6×), sin
inventar detalle ni sustituir la lectura: cada tamaño debe pasar el OCR real.

## Secuencia verificada

- Etapas 0–9: líneas aliadas inicialmente visibles; rivales sin título de campeón.
- Los nombres reales de la captura aparecen sucesivamente en el orden del draft;
  no se redibujan ni se inyectan como resultados del lector de texto.
- Dos lecturas para confirmar cada nombre nuevo; no puede confirmarse en la primera.
- La línea observada antes de seleccionar debe conservarse, sin inferirla por campeón.
- Los retratos de preselección permanecen visibles pero no pueden confirmar los nueve primeros.
- El décimo nombre permanece oculto; su identidad debe confirmarse visualmente.
- Ambos órdenes de equipo se distribuyen en la matriz. Red Magic/Poco prueban ambos.
- Geometrías equivalentes comparten píxeles conservando todos los modelos asociados.

## Evidencia y límites

La prueba se ejecuta en el APK release instalado, Android 34 x86_64 emulado,
con OCR real y sin aceptar sustituciones del reconocimiento. Se guarda por etapa
lo esperado y lo leído, geometría, modelo asociado, idioma y décimo resultado.
La matriz geométrica mide la calibración automática con cada perfil; el APK
instalado recibe esa calibración medida, no suplanta el fabricante del emulador.
Cuatro particiones conservan todos los fallos antes de generar una auditoría
única; una resolución o etapa ausente impide aprobarla y publicar la entrega.
La compilación usa otro ejecutor y puede avanzar mientras continúan las pruebas
completas. La publicación mantiene como requisitos tanto esas regresiones como
todas las pruebas del APK instalado; no se relaja ninguna comprobación.
Las tareas que generan capturas se vuelven a ejecutar explícitamente: restaurar
solo el XML de pruebas desde caché no equivale a recuperar sus evidencias gráficas.
Se mantiene la caché de compilación y no se omiten los reconocimientos.

Las etapas y etiquetas de líneas son sintéticas; los nombres seleccionados son
píxeles reales de la captura. No son partidas grabadas en cada modelo físico.
No se certifican todos los dispositivos Android, densidades, sistemas, latencias
o skins. S26 sigue siendo un escenario simulado, sin teléfono disponible.
Red Magic y Poco conservan la confirmación física previa del propietario.

## Archivos y verificaciones

- Matriz `DraftMultiDeviceAuditTest.kt` y generador/resumen de retratos.
- `DraftSequenceMultiDeviceInstalledTest.kt`: OCR productivo por etapa y décimo visual.
- `VisionCalibrationConfig.kt`, `DraftFrameGeometry.kt`, `AdaptiveScreenLayoutEngine.kt`,
  `DraftVisionScanner.kt`, `DraftNameAtlas.kt`, `DeviceDraftProfileTest.kt` y `DraftNameAtlasTest.kt`: bandas de nombres ajustadas al juego
  y ampliación del texto antes de reconocerlo.
- `scanner-sequence-plan.py`, `verify-draft-sequence-device.py`,
  `scanner-sequence-summary.py` y sus pruebas de integridad.
- Flujos de validación/publicación: exigen que todas las secuencias se completen.
- Política de reutilización: rechaza APK sin auditoría completa de secuencias.
- Versión y activos de prueba: las capturas no se incorporan al APK publicado.

Resultados locales: pruebas de generación/resumen e integridad de evidencias.
Pendientes de la ejecución: matriz Android, OCR nativo, pantallas en español y
portugués, firma persistente, ofuscación y funcionamiento del APK instalado.
Los resultados medidos de la ejecución prevalecen sobre los conteos planificados.
