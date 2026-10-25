# Coach 1.1.10.359 — build 1075

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
slot aliado 3 de la prueba de diez slots. El lector ahora prioriza un nombre de
campeón exacto cuando OCR mezcla ese nombre con una palabra de línea en la
misma fila; dos campeones distintos o dos líneas distintas continúan siendo
ambiguos.

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
