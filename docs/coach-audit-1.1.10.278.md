# Coach 1.1.10.278 — Selección aliada por nombre

- Versión anterior: 1.1.10.277, código 993.
- Versión nueva: 1.1.10.278, código 994.
- Problema encontrado: la supresión de insignias borraba una franja del título aliado cuando detectaba píxeles dorados. Los títulos dorados también pueden contener nombres reales de campeón o línea.
- Corrección: el atlas conserva una copia intacta de los cinco títulos aliados y otra con la supresión anterior. El nombre o línea exactos de la copia original tienen prioridad. Ambas variantes se leen en una sola solicitud OCR.
- Las coordenadas guardadas y los perfiles medidos del Red Magic 10 Pro y POCO X7 Pro se conservan.
- Pruebas agregadas: conservación de letras doradas en los dos perfiles; prioridad de lectura original; lectura rápida desde memoria vacía sobre las capturas reales Red Magic 7448/7449; primer pick dorado en ambos perfiles usando exclusivamente la ruta rápida.
- Verificación de entrega: pruebas Android, OCR real en APK instalado, ofuscación R8 y firma persistente mediante el flujo obligatorio de validación.

## Archivos

- app/build.gradle.kts
- app/src/main/java/com/example/service/screen/DraftNameAtlas.kt
- app/src/main/java/com/example/service/screen/AllyDraftNameReader.kt
- app/src/main/java/com/example/service/screen/DraftVisionScanner.kt
- app/src/test/java/com/example/DraftNameAtlasTest.kt
- app/src/test/java/com/example/AllyDraftNameReaderTest.kt
- app/src/androidTest/java/com/example/DraftRivalNameInstalledTest.kt

## Límite de la comprobación

Las capturas reproducen casos ya enviados y los dos perfiles conocidos; confirmar el resultado en los teléfonos físicos requiere una partida nueva. No se atribuye todavía todo el fallo reportado a una única causa.
