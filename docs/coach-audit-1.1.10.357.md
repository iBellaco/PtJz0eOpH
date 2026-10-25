# Coach 1.1.10.363 — build 1079

La ejecución anterior no llegó a probar el APK: el cambio de recuperación
OCR dejó una lambda no suspendible alrededor de `await()`, por lo que
`DraftVisionScanner.kt` no compiló. Además, el artefacto diagnóstico con
`if-no-files-found: error` ocultaba parcialmente ese error con un fallo
secundario cuando la compilación no generaba la carpeta.

Se corrigió la ejecución suspendible del OCR y el artefacto ahora avisa si no
existe, conservando como causa principal el error real de compilación.

La compilación posterior pasó, pero la prueba instalada R8 reveló un segundo
fallo: el escáner devolvía los campeones por slot, mientras el HUD los
descartaba si faltaba alguna etiqueta de línea. El HUD ahora conserva los
slots reconocidos y completa solo las posiciones aún vacías; no infiere la
identidad a partir de retratos ni altera la décima selección visual.

La ejecución siguiente confirmó ese caso y dejó pendiente `Malphite` en el
slot aliado 3 de la prueba de diez slots. La corrección queda limitada a una
palabra de línea y un nombre exacto en la misma fila física; dos campeones
distintos, dos líneas distintas o un nombre en la fila del invocador continúan
siendo ambiguos.

La comprobación anterior incluía por error todas las filas detectadas al medir
la distancia vertical. Ahora calcula la distancia únicamente sobre la fila de
título y conserva la regla general de ambigüedad fuera de ese contexto.

La regresión release confirmó después que `Malphite` sí estaba en el slot,
pero faltaba en el mapa por rol. Se restauró la reconciliación de roles: las
líneas observadas o recordadas no se alteran y solo los huecos sin etiqueta se
completan de forma determinista. `fix-error` ejecuta además la cobertura exacta
de los 142 nombres del catálogo.

La prueba aislada del APK release identificó el fallo de reconocimiento: en
`liveRedMagicCapturesWithMasteryBadgesAndViewerMasksFillTheActualHub`, el modo
rápido devolvía solo el slot aliado 0 (`Senna`) y omitía `Fizz`, `Nami` y
`Malphite`. El segundo caso instalado no podía confirmar Malphite por la misma
lectura incompleta.

La corrección mantiene el OCR aislado por bandas y la geometría de los slots,
pero permite una lectura completa de pantalla como recuperación cuando una
banda queda cubierta por una máscara del visor. El texto completo se somete a
las mismas reglas de columna, fila y título completo; no usa retratos, nombres
de invocador ni afinidad de campeón para inventar selecciones.

La cobertura de los 142 títulos del catálogo permanece en
`ChampionNameResolverCoverageTest`. Las pruebas de imágenes/emulador siguen
separadas de la evidencia física de Red Magic y POCO.

# Coach 1.1.10.364 — build 1080

La validación completa anterior completó 20 de 38 secuencias antes del límite
de ejecución. Sus cuatro particiones eran demasiado grandes para el OCR nativo
de ML Kit: los casos terminados tardaron entre seis y dieciséis minutos por
geometría. La matriz de teléfonos se distribuye ahora en dieciséis particiones,
con un máximo acotado de geometrías por emulador.

El caso Xiaomi Pad 6 agotó el límite de un frame. Esa tableta no pertenece al
alcance publicable del visor: el plan nativo ahora incluye exclusivamente
teléfonos Android donde puede ejecutarse el overlay, y excluye perfiles
genéricos, Apple y tabletas. Las transformaciones geométricas continúan en las
pruebas unitarias, sin presentarlas como certificaciones de teléfonos.

En POCO X7 Pro (captura 1920 × 864) y Xperia 1 V (captura 1920 × 822), la
evidencia diagnóstica leía el título completo `VI`, pero el recorte de
recuperación conservaba un límite vertical expresado en el atlas anterior y
rechazaba esa misma línea después de reempaquetarla. El recorte enfocado ya es
una banda exclusiva de título, por lo que ahora acepta toda su altura sin
invadir la fila del invocador. Ambos perfiles se añadieron al flujo aislado
`fix-error`; las regresiones release de Red Magic permanecen en su trabajo
aislado independiente.

La cobertura exacta de los 142 nombres sigue siendo obligatoria. Toda esta
evidencia corresponde a imágenes redimensionadas y APK instalado en emulador;
no se registra como prueba física de POCO, Xperia ni ningún otro modelo.

# Coach 1.1.10.365 — build 1081

La matriz completa encontró dos recuperaciones incompletas en capturas reales
redimensionadas: `VI` en Xperia 1 V (1920 × 822) y `JHIN` en Jelly Star
(854 × 480). Cuando ML Kit detectaba una caja parcial, la ruta de recuperación
probaba únicamente esas cajas y omitía la banda de título completa. Ahora la
banda calibrada completa se conserva siempre como último intento basado en los
mismos píxeles. La separación por color también se aplica a cualquier slot no
resuelto, incluido el título aliado dorado de Jhin.

La prueba aislada tenía además una condición que ejecutaba las tres capturas
reales solo para Red Magic y POCO durante el modo preflight. Los objetivos
explícitos de `fix-error` ahora fuerzan esas capturas para Xperia y Jelly Star,
de modo que el flujo corto reproduce exactamente el fallo que bloquearía la
validación completa. La cobertura de 142 nombres continúa siendo obligatoria.
Estas comprobaciones son de APK instalado sobre imágenes redimensionadas en
emulador; no se declaran como pruebas físicas de esos modelos.

# Coach 1.1.10.366 — build 1082

La primera corrección reprodujo ya las capturas reales, pero ambas excedieron
el límite de 60 segundos durante la recuperación: no fue un falso resultado de
Jhin o Vi, sino demasiadas solicitudes OCR secuenciales. La banda completa se
prueba ahora antes que las cajas parciales y las separaciones por color de los
slots pendientes se agrupan en páginas acotadas. Se mantienen los mismos
píxeles, límites de título y reglas de nombre completo, reduciendo llamadas sin
inyectar identidades. La validación aislada conserva el límite de 60 segundos
para detectar una regresión de rendimiento real.

# Coach 1.1.10.367 — build 1083

La ejecución aislada confirmó que Xperia ya completaba las capturas reales y
Jhin, pero seguía omitiendo Vi; Jelly Star todavía agotaba el límite al entrar
en esas capturas. El recorte enfocado terminaba antes de la fila del invocador,
pero conservaba un segundo límite que rechazaba el 30 % superior. ML Kit puede
ubicar allí el centro de títulos pequeños. La recuperación acepta ahora toda la
altura de esa banda exclusiva de título, manteniendo intactos sus límites
laterales y sin leer la fila del invocador. Esto permite resolver antes el
título y evita activar recuperaciones posteriores más costosas.

# Coach 1.1.10.368 — build 1084

Aceptar toda la altura del título no bastó mientras seguían activas las nuevas
llamadas de recuperación: Jelly Star y Xperia agotaron 60 segundos en la
primera captura real. Se retiraron esas llamadas adicionales y se mantuvo solo
el límite vertical corregido. Cuando una captura real no coincide, la prueba
aislada registra ahora el texto y las cajas que devuelve directamente ML Kit
sobre esos píxeles redimensionados. Esa evidencia es exclusivamente
diagnóstica: no alimenta el estado del escáner ni sustituye nombres esperados.

# Coach 1.1.10.369 — build 1085

La evidencia directa eliminó la ambigüedad. En 854 × 480 ML Kit devolvió
`HIN` como toda la fila de Jhin porque el emblema de maestría cubre el trazo de
la J. Esa lectura observada se admite únicamente en la banda de título; sigue
rechazada dentro de apodos o frases. En 1920 × 822 ML Kit no produjo ninguna
línea para `VI`. La variante que separa el color del título aliado se incorpora
ahora como otro recorte de la misma página OCR ya existente, sin una nueva
invocación, sin retratos y sin ampliar la banda hacia el invocador. Las pruebas
negativas impiden que `XCS HIN` o `Mi HIN` confirmen un campeón.

# Coach 1.1.10.370 — build 1086

La ejecución confirmó Jelly Star y dejó como único fallo Xperia 1 V: los
píxeles de `VI` estaban intactos, pero el título ocupaba una porción mínima de
la alternativa tras borrar el emblema. La supresión devuelve ahora el límite
real del componente eliminado. Solo la alternativa sin emblema recorta ese
margen negro y usa una ventana compacta; las variantes intacta y de contraste
conservan toda la banda para nombres largos. No se añade una solicitud OCR, no
se usa el retrato y no se infiere la identidad por la posición del slot.

# Coach 1.1.10.371 — build 1087

La validación completa confirmó el reconocimiento anterior y encontró una
regresión de arranque en `sequence-42-ally-first` (captura 1920 × 863,
perfiles Xiaomi 14 y Nothing Phone 2a). La primera carga del modelo local de
ML Kit tardó 74 s; el siguiente frame tardó 60 s y el primer pick agotó su
límite de 60 s antes de devolver un resultado. El servicio precarga ahora el
reconocedor con una imagen negra de 32 × 32, sin texto del juego, antes de
recibir el primer frame grande. La prueba instalada ejecuta la misma precarga
fuera del presupuesto de cada frame y conserva sin cambios el límite de 60 s.

Como prevención adicional, los textos localizados `Jugador`, `Jogador` y
`Player`, seguidos exactamente de los slots 1–5, se clasifican como puestos
aún no seleccionados. No son alias de campeones y no pueden confirmar una
identidad; evitan recuperaciones OCR innecesarias en capturas reales donde el
juego todavía muestra esos placeholders.

El flujo aislado reproduce los diez pasos de esa geometría con el mismo límite
de 60 segundos, además de conservar las pruebas unitarias y la cobertura exacta
de los 142 nombres. La evidencia sigue siendo un APK instalado en emulador con
capturas redimensionadas; Xiaomi 14 y Phone (2a) no se registran como pruebas
físicas.

# Coach 1.1.10.372 — build 1088

La ejecución aislada demostró que la precarga de 32 × 32 no eliminaba el coste
principal: cada frame de `sequence-42-ally-first` seguía enviando primero la
captura completa de 1920 × 863 a ML Kit. Los dos frames vacíos tardaron 76,6 s
y 57,6 s, y el primer pick volvió a agotar 60 s. Ese OCR de pantalla completa
era redundante en el modo rápido `namesOnly`, porque los diez nombres y líneas
ya se leen desde el atlas calibrado de títulos.

El modo rápido omite ahora únicamente esa solicitud global y conserva el OCR
global en los escaneos completos que necesitan banners, identidad y metadatos.
Los títulos, las dos confirmaciones temporales y las recuperaciones de píxeles
siguen intactos. También se conserva explícitamente el estado `unpicked` leído
en el atlas. La prueba aislada mantiene el límite original de 60 segundos para
comprobar la mejora real; continúa siendo una prueba instalada en emulador con
capturas redimensionadas, no una prueba física de Xiaomi o Nothing.

# Coach 1.1.10.373 — build 1089

El preflight release con R8 encontró un fallo distinto en
`sequence-56-ally-first`: sobre la geometría 1920 × 924 asociada al perfil P20,
la captura real `draft-mel-nine.jpg` reconoció ocho títulos pero omitió `VI`.
Al faltar el noveno nombre, la política correcta bloqueó también el décimo pick
visual de Teemo. El OCR directo confirmó que ML Kit no devolvía ni el nombre ni
una caja de texto para esa fila; no fue una asignación a otro campeón.

La recuperación final por color, que ya trabaja exclusivamente con píxeles de
la banda calibrada, se aplica ahora también a títulos aliados pendientes. Usa la
alternativa sin emblema de maestría, conserva ambos glifos y agrupa todos los
pendientes en una sola solicitud acotada. No acepta abreviaturas, retratos ni
identidades esperadas. El flujo aislado incorpora exactamente P20 1920 × 924 y
lo ejecuta sobre APK release con R8. La evidencia continúa siendo una captura
redimensionada instalada en emulador, no una prueba física del P20.
