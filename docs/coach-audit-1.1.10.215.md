# Auditoría de Coach — 1.1.10.215 (build 931)

## Alcance y continuidad

Continúa la revisión 1.1.10.214 del PR 59. Los consejos core, runas, hechizos y las 14 botas usadas por las 300 builds tienen decisiones específicas. La revisión anterior pasó compilación release, ofuscación, firma persistente, pruebas locales y pruebas del APK instalado en español y portugués en la ejecución 37521424161.

## Correcciones adicionales

- Los core muestran siempre su momento de compra específico; la recomendación situacional ya no reemplaza ese texto.
- Las recomendaciones de relleno se filtran antes de traducir. La prueba de interfaz comprueba que los core tampoco muestren amenazas genéricas en portugués.
- Las notas guardadas corrigen Leyenda: Velocidad (aceleración de habilidades), Fortalecimiento (tres ataques y amplificación), Fuente de Vida (ataque/habilidad junto al aliado herido) y Orbe Anulador (escudo al caer de vida). Botas de mercurio y Trituradoras encadenadas dejan de usar una política de movilidad genérica. Se regeneran los textos y sus traducciones sin cambiar las elecciones de las builds.
- El consejo de un hechizo describe su efecto en vez de presentar únicamente el encabezado de mapas bajo «Qué aporta el hechizo».
- Las botas se resuelven por su identidad exacta antes de recurrir al catálogo general. El consejo sigue la selección real de nivel 2 y la evolución correspondiente de nivel 3, incluyendo al volver a la bota principal.
- Los mapas aparecen como etiquetas en cuadrícula, lista, detalle del catálogo y detalle de hechizos de una build. Se conserva el texto mecánico completo.
- Las tarjetas de cuadrícula adaptan su altura a las etiquetas y el tamaño de texto. Se define la altura de línea de las etiquetas y se comprueba que el enfriamiento siga visible en ambas vistas.
- El formato reconoce encabezados singulares y plurales en español y portugués, normaliza Abismo de los Lamentos/Abismo dos Lamentos/Howling Abyss y conserva mapas desconocidos.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/util/BuildElementAdvice.kt`
- `app/src/main/java/com/example/util/SpellCatalogFormatting.kt`
- `app/src/main/java/com/example/ui/components/SpellMapLabels.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/ui/screens/MetaAndDraftScreen.kt`
- `app/src/test/java/com/example/BuildElementAdviceTest.kt`
- `app/src/test/java/com/example/BuildCoachingRenderedTest.kt`
- `app/src/test/java/com/example/SpellCatalogFormattingTest.kt`
- `app/src/test/java/com/example/SpellCatalogRenderedTest.kt`
- `.github/workflows/build-apk.yml`
- `tools/build_coaching.py`
- `app/src/main/assets/champions_creator_builds.json`
- `app/src/main/assets/translations_pt.json`
- `app/src/test/java/com/example/BuildCoachingRegressionTest.kt`
- Este reporte.

## Verificación

- Comprobación de datos generados para 142 campeones y 300 builds: `python3 tools/update-champion-builds.py --check`.
- Auditoría de texto portugués: `bash tools/audit-portuguese.sh`.
- Pruebas de formato sobre todos los hechizos reales en ambos idiomas, casos singulares, conjunciones y descripciones sin mapas.
- Regresión de tiempo de compra core y consejos de hechizos sin encabezados de mapas.
- Pruebas de interfaz de Vi: Grebas codiciosas → Botas de mercurio → Trituradoras encadenadas → regreso a Botas inmortales, en español y portugués.
- Pruebas de interfaz del catálogo en cuadrícula, lista y modal, con capturas en ambos idiomas.
- El flujo del PR ejecuta las pruebas locales de regresión, compilación release con R8, firma persistente y auditorías del APK instalado antes de fusionar. El resultado definitivo se consulta en GitHub Actions; este reporte no sustituye la ejecución.
