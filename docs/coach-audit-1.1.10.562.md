# Coach 1.1.10.562 — temas Premium de aplicación, China y consejos compactos

## Versión

- `versionCode`: 1278
- `versionName`: 1.1.10.562
- Rama de trabajo: `fix-error`

## Cambios validados

- Se añadieron siete temas independientes de las regiones: White, Black, Aurora, Ember, Sakura, Forest y Cobalt.
- Cada tema de aplicación muestra su paleta, la vista previa y la etiqueta `PREMIUM` antes de aplicar.
- White usa fondo claro con texto oscuro de alto contraste; los demás estilos mantienen colores legibles en superficies profundas.
- Los controles del servidor China usan una paleta propia roja y dorada, sin reutilizar el morado de los controles regionales.
- Las tarjetas de recomendación usan el consejo táctico específico del catálogo de cada campeón.
- Los consejos largos se muestran resumidos y tienen expansión individual para no ocultar la información de las tarjetas siguientes.
- Se incorporaron las traducciones de los nuevos textos al catálogo portugués.

## Archivos principales

- `app/src/main/java/com/example/ui/theme/AppThemeManager.kt`
- `app/src/main/java/com/example/ui/theme/Theme.kt`
- `app/src/main/java/com/example/ui/components/ThemeCustomizationDialog.kt`
- `app/src/main/java/com/example/ui/components/DraftAdviceSection.kt`
- `app/src/main/java/com/example/ui/screens/DraftAnalysisTab.kt`
- `app/src/main/java/com/example/ui/screens/TierListTab.kt`
- `app/src/main/java/com/example/util/ChampionMatchupCoaching.kt`

## Verificación

- `jq empty app/src/main/assets/translations_ui_pt.json`: correcto.
- `git diff --check`: correcto.
- Pendiente de completar con `./gradlew :app:assembleRelease` y las pruebas unitarias del proyecto.
