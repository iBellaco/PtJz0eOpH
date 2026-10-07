# Auditoría de Coach — 1.1.10.218 (build 934)

## Cambios

- Catálogo de hechizos: retirados la altura fija de 184 dp y el reparto con grandes separaciones. Las tarjetas tienen un mínimo de 112 dp y crecen si lo requiere el texto; las etiquetas de mapas siguen dentro de las descripciones.
- Ventaja, debilidad y sinergia: tres secciones con filas de tres campeones. La misma política limita cada sección a 3 para invitados, 6 para registrados y 12 para premium. Se preserva el orden del perfil, sin repetir nombres ni incluir al propio campeón.
- Al pulsar una relación se muestra solamente su nombre. Se retira el texto «Cerrar»; el nombre y el área exterior permiten descartar la vista, sin redirigir a otra build.
- Aplastar: retirados 54 consejos guardados del catálogo de builds. Se oculta también cualquier consejo antiguo o generado para Aplastar, Castigo, Smite y Golpear en las vistas de builds y sus detalles. Se conserva la descripción mecánica del hechizo.
- El generador de builds respeta la exclusión de Aplastar y elimina sus traducciones de consejos obsoletas para que no vuelvan a aparecer al regenerar el catálogo.
- El aviso de recuperación de la cuenta cambia suavemente entre tonos rojos, con variantes legibles para superficies claras y oscuras. No cambia el plazo ni el procedimiento de eliminación.

## Comprobaciones

- Se amplía la cobertura para verificar los perfiles reales de los 142 campeones y sus 300 combinaciones de línea, incluyendo 3/6/12 relaciones por sección y filas de tres.
- Se conservan las relaciones revisadas que incluyen amenazas de otras líneas; se verifica su existencia en el catálogo sin reinterpretarlas como duelos exclusivos de la misma línea.
- Las pruebas de interfaz verifican los tres niveles de acceso en ambos idiomas, nombres sin navegación ni texto de cierre y tarjetas de hechizos sin la altura vacía anterior.
- Se comprueba que Aplastar no recupere consejos desde datos guardados, alias o alternativas generadas y que otros hechizos conserven sus consejos.
- Los resultados definitivos de compilación, firma, ofuscación y APK instalado se incorporan a la auditoría de entrega después de terminar CI. No se declaran aprobados antes de ejecutarlos.

## Archivos modificados

- `.github/workflows/build-apk.yml`
- `README.md`
- `app/build.gradle.kts`
- `app/src/main/assets/champions_creator_builds.json`
- `app/src/main/assets/translations_pt.json`
- `app/src/main/java/com/example/ui/components/AccountDeletionCard.kt`
- `app/src/main/java/com/example/ui/components/CustomBuildDetailDialog.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/ui/screens/MetaAndDraftScreen.kt`
- `app/src/main/java/com/example/util/BuildChoiceRules.kt`
- `app/src/main/java/com/example/util/BuildElementAdvice.kt`
- `app/src/test/java/com/example/BuildCoachingRegressionTest.kt`
- `app/src/test/java/com/example/ChampionBuildsCatalogValidationTest.kt`
- `app/src/test/java/com/example/BuildCoachingRenderedTest.kt`
- `app/src/test/java/com/example/BuildElementAdviceCoverageTest.kt`
- `app/src/test/java/com/example/BuildElementAdviceTest.kt`
- `app/src/test/java/com/example/CoachMatchupCoverageTest.kt`
- `app/src/test/java/com/example/SpellCatalogRenderedTest.kt`
- `tools/build_coaching.py`
