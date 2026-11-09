# Auditoría de localización portuguesa de Coach 1.1.10.496

## Versión

- `versionName`: `1.1.10.496`
- `versionCode`: `1212`
- Alcance: completar la traducción de la interfaz en portugués brasileño y
  mantener la terminología de League of Legends: Wild Rift.

## Problema corregido

La selección de portugués todavía mostraba etiquetas en español y algunas
formas de portugués europeo en pantallas, mensajes generados y descripciones
del overlay. La auditoría anterior no detectaba varios cognados que podían
pasar como portugués.

## Cambios realizados

- Normalización centralizada a portugués brasileño para textos heredados,
  incluidos `arquivo`, `usuário`, `aplicativo`, `assinar`, `itens`, `atual` y
  `porcentagem`.
- Traducción de etiquetas de actividad, anuncios, patrocinio, perfiles,
  exportación/importación, correo, almacenamiento, resultados, suscripciones,
  builds, videos y métricas CPM/RPM.
- Terminología de Wild Rift actualizada para rutas, banimentos, itens,
  feitiços de invocador y clasificaciones.
- Descripción accesible del logo del overlay traducida según el idioma activo.
- Auditoría de residuos ampliada para detectar español y portugués europeo que
  antes no se marcaban.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/assets/translations_ui_pt.json`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/main/java/com/example/util/TranslationCatalog.kt`
- `app/src/test/java/com/example/OfflinePortugueseAuditTest.kt`
- `app/src/test/java/com/example/SpanishUiResidue.kt`

## Verificaciones

- `bash tools/audit-portuguese.sh`: correcto; sin hallazgos de español en el
  catálogo fuente.
- `python3 -m unittest discover -s .github/scripts -p
  'test_localized_resources.py'`: 6 pruebas correctas.
- Suite selectiva de localización y regresión de Coach: correcta.
- JSON de traducciones validado con `python3 -m json.tool`.
- `git diff --check`: correcto.

La compilación release firmada y la validación en dispositivos físicos quedan
pendientes de ejecutarse en el entorno con la firma de producción disponible.
