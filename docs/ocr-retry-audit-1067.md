# Coach — corrección de reintentos OCR, entrega 1.1.10.352 / 1068

## Evidencia consultada

Ejecución real: https://github.com/iBellaco/PtJz0eOpH/actions/runs/40104597791
Revisión: `6d6cfa248ceecaaea03de4512f315e09cbbd760c`.
Trabajo fallido: `native-title-preflight`, ID `120910324007`.

- 640 × 360, secuencia 4: faltaban Mel, Volibear y Vi. El diagnóstico
  `lineFocusedDiagnostics` sí leyó MEL en el segundo recorte, que producción
  descartaba al usar únicamente `take(1)`.
- 1920 × 924, secuencia 56 (perfil de pantalla P20): faltaba Vi. IA/rVI no son
  títulos completos y no se aceptan como identidades.
- 1920 × 868, secuencia 61 (perfil Honor X8a): venció el límite de 60 segundos.
  La evidencia antigua no registró qué fotograma agotó el tiempo.
- Catálogo, seguridad, pruebas unitarias, geometría simulada, compilación e
  instalación ES/PT aprobaron en esa revisión. La publicación fue bloqueada
  correctamente por la prueba nativa. Esto no certifica esta nueva revisión.

## Cambios y prevención

- `DraftNameAtlas`: conserva dos cajas medidas distintas y la fila calibrada;
  añade tamaños de texto 24/36/48 y páginas de alternativas sin reescalar sus
  píxeles al agruparlas. Conserva origen, límites de título y exclusiones.
- `DraftVisionScanner`: aplica primero alternativas multiescala en grupos de
  hasta dos slots para limitar memoria. Omite páginas de slots ya resueltos;
  combina recortes enfocados y sin insignia. Lee el recorte medido antes de
  normalizarlo. No elimina los reintentos de color de aliados ni la escena
  original por tratarse de 640 × 360.
- `AllyDraftNameReader`: elimina el atajo especial de Vi que podía saltarse la
  separación entre título e invocador. Todas las variantes usan la misma regla
  de filas y nombre completo. No se agregan equivalencias IA/rVI.
- Se propaga la cancelación: no se transforma en un resultado parcial exitoso.
- `DraftSequenceMultiDeviceInstalledTest`: registra etapa, fotograma, duración,
  tipo de excepción e imagen aun si se agota el tiempo. Mantiene los 60 segundos,
  los nueve títulos, dos fotogramas y la confirmación visual del décimo.
- Regresiones en `DraftNameAtlasTest` y `AllyDraftNameReaderTest`: segundo recorte,
  deduplicación, fila alternativa, tamaños, máscaras, píxeles, propiedad de los
  bitmaps y rechazo de nombres cortos fuera de la fila del título.

## Validación

`python3 -m unittest discover -s tools -p 'test_scanner_*.py'`: 14 pruebas aprobadas.
Pruebas de bloqueo/reutilización del APK e informe de fallos: 17 aprobadas.
Pruebas de reintento de auditoría y resumen JUnit: 4 aprobadas.
`git diff --check`: aprobado.

Pruebas Kotlin locales: `DraftNameAtlasTest` (23) y `AllyDraftNameReaderTest` (9),
32 aprobadas, cero fallos y cero omisiones. Compilación de la aplicación debug,
pruebas unitarias y `compileDebugAndroidTestKotlin`: aprobadas.
Se usó configuración local ficticia de servicios solo para generar recursos
debug, sin credenciales ni cuentas reales; no se incorpora al commit ni al APK
de entrega. Esto no sustituye las pruebas de integración ni la firma release.

Las secuencias nativas de esta revisión siguen pendientes.
No se han modificado las barreras de publicación ni los umbrales para obtener un
verde artificial. La versión de esta corrección es 1.1.10.352 / 1068; el APK
1067 no se reutiliza porque todavía fallaba en la matriz nativa.

Estas medidas son capturas simuladas, no pruebas físicas de P20 ni Honor X8a.
Red Magic y POCO conservan únicamente la verificación física previa informada
por el propietario; no certifica este cambio. No hay S26 conectado.

## Corrección posterior — 1.1.10.352 / 1068

La ejecución `40110730898` confirmó que la pasada multiescala global todavía
provocaba timeout a 640 × 360 y que Vi seguía faltando a 1920 × 924. Se elimina
esa pasada. La recuperación ahora agrupa solo la banda enfocada y su alternativa
sin insignia; después usa como máximo dos cajas OCR distintas y no agrega una
pasada de color para aliados. Esto limita las solicitudes por fotograma y evita
que la lectura de un slot bloquee todo el escaneo.

La ejecución también evidenció un timeout inicial a 1920 × 868; queda cubierto
por el mismo límite de reintentos. El timeout no se convierte en éxito ni se
eleva artificialmente el límite de 60 segundos.

## Gate rápido de validación

La siguiente ejecución separa un gate previo con los tres casos que bloquearon
la revisión: `sequence-4-ally-first` (640 × 360), `sequence-56-ally-first`
(1920 × 924) y `sequence-61-enemy-first` (1920 × 868). Si uno falla, la matriz
completa no comienza y el informe conserva la captura y el fotograma fallido.
Si los tres pasan, se ejecutan igualmente todas las secuencias y controles de
publicación. Este gate no autoriza por sí mismo una release.
