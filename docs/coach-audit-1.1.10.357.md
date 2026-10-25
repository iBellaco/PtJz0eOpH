# Coach 1.1.10.357 — build 1073

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
