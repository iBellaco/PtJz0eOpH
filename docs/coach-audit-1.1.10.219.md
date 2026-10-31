# Auditoría de Coach — 1.1.10.219 (build 935)

## Estado de la intervención

La versión 1.1.10.219 se validó, fusionó y publicó. Este documento conservaba por error un estado previo a la entrega; se corrige en la revisión 220. El propietario confirma haber creado COACH_GOOGLE_SERVICES_JSON. La conexión recibió HTTP 403 al intentar crearlo, pero ese rechazo no es un fallo actual del Secret: el run 37703548670 restauró correctamente la configuración en los dos trabajos consumidores y aprobó la compilación y la validación instalada en ES/PT. El run 37706124268 publicó el APK de la misma fuente validada. PR 64, commit de entrega b7abd56707f4a013f7f7556a5782270860eb7a3c; versión 1.1.10.219, build 935, paquete instalado com.Coach. APK SHA-256: b4f39c0287647c65f13dbc08ef54dfcda326a72ad8f09710f3839c9b2ea939b1. La validación documentada comprende las pantallas examinadas; no demuestra que todos los textos de la aplicación estén traducidos.

## Hallazgos contrastados con el repositorio actual

- El workflow contenía una configuración Android completa en texto plano; se reemplaza por restauración desde el Secret, validada para com.Coach, sin imprimir sus valores. La ausencia del Secret detiene la compilación.
- .env.example no contenía una URL ni una clave real de Supabase en esta revisión. Se elimina la configuración ficticia de Gemini, que no tenía consumidores; .env.example conserva únicamente instrucciones para los valores locales privados. No se encuentran referencias activas al proveedor obsoleto.
- auto_scraper.yml ya no existía. No se informa de su eliminación como cambio nuevo.
- BestBuildWrScraper.kt ya consultaba /tierlist, además de WildRiftFire y WildRiftCore. No se cambia /champions por /tierlist porque no correspondía al código actual.
- ChineseMetaSyncService.kt ya no consultaba el endpoint de Tencent descrito. Se retiran sus nombres, tipos de rango y rutas de datos sin uso, sustituyendo la coordinación activa por GlobalMetaSyncService y MetaSyncState.
- Se retiran dos traducciones obsoletas que atribuían los datos a Tencent/LOLM. Las preferencias antiguas CN se siguen migrando a GLOBAL. El campo serializado cnTier permanece por compatibilidad; la sincronización no lo usa ni lo rellena.
- Se detectó una conversión de categorías en porcentajes sintéticos cuando faltaban números en el catálogo. Se elimina: cambiar una categoría conserva los números originales, incluidos ceros. Esto no certifica la exactitud de los porcentajes ya incluidos en los recursos.
- La raíz tenía 39 archivos versionados, de los cuales se retiran 25 scripts e informes de reparación sin referencias en los flujos ni herramientas mantenidas. No eran 200 archivos en esta revisión.
- El README actual ya no declaraba el proyecto código abierto. Se aclara su finalidad de compilación y respaldo, y que la visibilidad pública no concede una licencia general. No se incorpora una licencia de software libre.

## Organización del código

Se separan secciones del catálogo, draft, historial, gestión de cuentas y avisos, informes y asistente flotante en archivos del mismo paquete. Se mantienen los cuerpos de las funciones; los auxiliares compartidos pasan de privados de archivo a internos del módulo cuando necesitan llamadas desde otro archivo. El auxiliar compartido createUserManagementApprovalRequest recibe un nombre específico para evitar una colisión con otro auxiliar privado de moderación. Se retiran cuatro composables o auxiliares sin llamadas. No se modifica el catálogo de builds ni sus límites de relaciones de la versión 218.

Archivos principales después de dividirlos: MetaAndDraftScreen.kt, 782 líneas; AdminDashboardDialog.kt, 451; AdminFeedbackPanel.kt, 1252; FloatingAssistantService.kt, 806; DraftHistoryScreen.kt, 1855. ChampionDetailSheet.kt queda por debajo de 2000 líneas. WildRiftItemsData.kt sigue teniendo 2601 líneas de catálogo generado; no se divide para conservar el contrato de las herramientas de generación.

## Comprobaciones ejecutadas

- Treinta pruebas Python de scripts de compilación aprobadas, incluidas cuatro de restauración: archivo válido, Secret ausente, entrada malformada sin filtrar su contenido y paquete incorrecto. Todas aprobadas.
- Generadores de componentes, iconos y builds en modo --check: aprobados; 142 campeones, 300 perfiles de línea y 52 iconos locales.
- Auditoría estática de portugués: 2536 apariciones de textos visibles, sin hallazgos de español.
- Análisis de sintaxis Kotlin mediante el PSI del compilador 2.2.10: 293 archivos, sin errores de sintaxis. Esta comprobación no reemplaza la compilación con resolución de tipos.
- Comparación de 75 declaraciones trasladadas de cinco archivos: cuerpos conservados, descontando visibilidad y espacios. Comparación adicional de 22 declaraciones del catálogo y draft: sin diferencias de comportamiento fuera de retirar los colectores de rango obsoletos; se descuentan comentarios de sección y cambios de visibilidad en la comparación.
- Workflow YAML: los dos trabajos consumidores usan el Secret, sin valores de configuración incrustados. Búsqueda de las claves cliente originales en los archivos versionados actuales: cero coincidencias; los archivos locales privados están excluidos de Git.
- Pruebas de pantallas del workflow ejecutadas localmente: 303 pruebas aprobadas, cero fallos, errores u omisiones, incluidas variantes en español y portugués. Sus comprobaciones de comportamiento confirmaron 3/6/12 relaciones por categoría en los 142 campeones y 300 perfiles, y coherencia de 6246 recomendaciones de línea. Se generan capturas y textos renderizados para catálogo, builds, draft, historial, asistente, cuentas y avisos. Son pruebas de componentes y comportamiento con Robolectric; no equivalen a instalar el APK release en un dispositivo.
- git diff --check: aprobado.
- Compilación local del código principal Kotlin y Java: aprobada después de corregir la colisión del auxiliar y retirar una propiedad opcional sin consumidores. Se usan Java 21, Gradle 9.3.1 y SDK 36 preparados fuera del repositorio, con el archivo cliente local excluido de Git.
- Se agregan dos regresiones sobre la conservación de los porcentajes y la ausencia de estadísticas sintetizadas. Resultados JUnit locales: 138 pruebas en 21 clases, cero fallos, errores u omisiones. Incluyen las dos nuevas regresiones y la conservación de las migraciones de preferencias antiguas.

## Límites y riesgos pendientes

- El rechazo de la creación desde esta conexión es histórico. El propietario creó el Secret y Actions confirmó su disponibilidad y validez; no está pendiente volver a crearlo. El archivo local privado no está versionado.
- Retirar los valores del workflow no los borra de commits ni registros históricos. La configuración cliente continúa incluida en el APK y no sustituye las reglas de acceso. No se rotaron claves ni se reescribió el historial.
- La refactorización se compiló y validó antes de publicar. La primera compilación detectó una colisión de nombres al compartir un auxiliar y una constante Java inválida al dejar una propiedad opcional vacía. Se corrigieron ambas antes de las comprobaciones finales aprobadas: 138 pruebas de núcleo, 303 de pantallas, APK release con 1249 clases renombradas por R8 y recorridos instalados de 36 pantallas ES y 39 PT, más cinco casos controlados de eliminación de cuenta.
- La autorización de Riot y la aceptación de Google Play siguen sin estar acreditadas; esta limpieza no las concede. La auditoría legal previa y su guía siguen aplicando.
- Un nombre extraño no restringe el acceso a un repositorio público. No se cambió su visibilidad ni se eliminó el repositorio.

## Archivos modificados, añadidos o retirados

- `.env.example`
- `.github/scripts/restore-google-services.py`
- `.github/scripts/test_google_services.py`
- `.github/workflows/build-apk.yml`
- `.gitignore`
- `README.md`
- `all_missing_keys.json`
- `app/build.gradle.kts`
- `app/src/main/assets/translations_pt.json`
- `app/src/main/java/com/example/MainActivity.kt`
- `app/src/main/java/com/example/WildRiftApp.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/data/sync/ChineseMetaSyncService.kt`
- `app/src/main/java/com/example/data/sync/ChineseSyncState.kt`
- `app/src/main/java/com/example/data/sync/GlobalMetaSyncService.kt`
- `app/src/main/java/com/example/data/sync/MetaSyncState.kt`
- `app/src/main/java/com/example/data/sync/TencentRankTier.kt`
- `app/src/main/java/com/example/model/Champion.kt`
- `app/src/main/java/com/example/service/FloatingAssistantService.kt`
- `app/src/main/java/com/example/service/FloatingDraftCoachView.kt`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/main/java/com/example/service/FloatingSaveMatchDialog.kt`
- `app/src/main/java/com/example/service/OverlayState.kt`
- `app/src/main/java/com/example/ui/components/AdminDashboardDialog.kt`
- `app/src/main/java/com/example/ui/components/AdminFeedbackPanel.kt`
- `app/src/main/java/com/example/ui/components/AdminNoticeConfigDialog.kt`
- `app/src/main/java/com/example/ui/components/AvatarGiftDialog.kt`
- `app/src/main/java/com/example/ui/components/BroadcastAnnouncementDialog.kt`
- `app/src/main/java/com/example/ui/components/BuildSuggestionParser.kt`
- `app/src/main/java/com/example/ui/components/BuildSuggestionPreview.kt`
- `app/src/main/java/com/example/ui/components/FeedbackReportCard.kt`
- `app/src/main/java/com/example/ui/components/ModeratorRequestsDialog.kt`
- `app/src/main/java/com/example/ui/components/UserDetailManagementDialog.kt`
- `app/src/main/java/com/example/ui/components/UserManagementActions.kt`
- `app/src/main/java/com/example/ui/components/UserManagementPanel.kt`
- `app/src/main/java/com/example/ui/screens/ChampionBuildDetails.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/ui/screens/ChampionsCatalogTab.kt`
- `app/src/main/java/com/example/ui/screens/DraftAnalysisTab.kt`
- `app/src/main/java/com/example/ui/screens/DraftHistoryScreen.kt`
- `app/src/main/java/com/example/ui/screens/ItemsCatalogTab.kt`
- `app/src/main/java/com/example/ui/screens/MapObjectivesTab.kt`
- `app/src/main/java/com/example/ui/screens/MetaAndDraftScreen.kt`
- `app/src/main/java/com/example/ui/screens/RunesTab.kt`
- `app/src/main/java/com/example/ui/screens/SavedDraftCard.kt`
- `app/src/main/java/com/example/ui/screens/SpellsTab.kt`
- `app/src/main/java/com/example/ui/screens/TierListTab.kt`
- `app/src/test/java/com/example/MetaRegionRegressionTest.kt`
- `clean_missing_pt.json`
- `do_fix.py`
- `docs/coach-audit-1.1.10.219.md`
- `filtered_ui_missing.json`
- `fix_data.py`
- `fix_unpicked.sh`
- `generate_all_champion_builds.py`
- `generate_full_advisor.py`
- `generate_kotlin_matchups.py`
- `generate_matchups.py`
- `missing_strings_full.json`
- `patch_legendary_cache.sh`
- `patch_legendary_logic.sh`
- `pt_translations_bulk.json`
- `test_matchup_rules.py`
- `test_scraper.py`
- `translate_keys.py`
- `untranslated_strings.txt`
- `update_admin_btn.py`
- `update_all_matchups.py`
- `update_app_version.py`
- `update_assassin.py`
- `update_auth_card.py`
- `view_admin_btn.py`
- `view_auth.py`
