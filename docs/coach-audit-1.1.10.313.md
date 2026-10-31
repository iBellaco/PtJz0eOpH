# Coach 1.1.10.313 (build 1029)

## Corrección de validación

- La regla única de compra de objetos situacionales ahora usa el catálogo bilingüe.
- Se conserva el texto español para el idioma español y se muestra la traducción portuguesa en portugués.
- Se evita que la auditoría de textos visibles detecte español dentro de una rama portuguesa.

## Archivos

- `app/build.gradle.kts`
- `app/src/main/assets/translations_pt.json`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`

## Validación

- El cambio está preparado para volver a ejecutar la auditoría de portugués y el workflow de APK.
