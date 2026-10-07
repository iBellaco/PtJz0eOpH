# Auditoría de Coach — 1.1.10.216 (build 932)

## Problemas observados

La captura presenta etiquetas antiguas Corki/Yunara junto a los nombres actuales Caitlyn/Milio. El HUD rival se actualizaba fila por fila y podía conservar campeones de una lectura anterior. El visor podía confirmar el décimo pick sin aplicar inmediatamente esa decisión a la selección visible. Las etiquetas de mapas ampliaban las tarjetas principales del catálogo. Los iconos de emparejamientos abrían otra build.

## Cambios

- Lectura ampliada de la banda de nombre de cada slot rival, igual que en aliados. Los recortes pertenecen a una sola fila, excluyen la ventana y los textos de depuración; una lectura de nombres contradictorios no confirma un candidato dirigido.
- Aplicación atómica de la lista rival por roles: elimina previews anteriores, evita duplicados y respeta elecciones manuales y la identidad del equipo aliado.
- Los nueve picks previos proceden de una sola lectura compatible. Mezclar un preview anterior con su reemplazo ya no aumenta artificialmente el número de campeones.
- El resolvedor de nombres deja de fabricar fichas con rol/estadísticas genéricas cuando no encuentra una entrada real. Sin identidad disponible, el resultado permanece pendiente.
- El HUD recibe las decisiones confirmadas del visor incluso con el escaneo pausado. El escaneo dirigido usa la misma regla segura de aplicación del décimo pick: llenar una vacante, preservar los otros campeones y las selecciones manuales.
- Los iconos de ventaja, debilidad y sinergia muestran el nombre localizado al tocarlos; no navegan a otra build. Los controles reservan 48 dp, con filas adaptadas al espacio compacto.
- Las etiquetas de mapas se conservan únicamente dentro de la descripción del hechizo, fuera de la cuadrícula y la lista del catálogo. La cuadrícula recupera su altura fija y conserva el enfriamiento visible.

## Verificación

- Datos de 142 campeones y 300 builds comprobados sin cambios en sus elecciones.
- Auditoría de portugués aprobada: cero hallazgos de texto español visible.
- Regresiones del HUD para reemplazos Caitlyn/Milio, selección de Yunara, bloqueos manuales, recuento sin previews antiguos y aplicación del décimo campeón.
- Pruebas de interfaz de los tres grupos de emparejamientos y del catálogo, en español y portugués.
- Pruebas del lector OCR instalado sobre recortes reales de las bandas Caitlyn/Milio de la captura. Los recortes solo contienen nombres de campeones y etiquetas genéricas de jugador.
- El flujo de validación ejecuta compilación release, ofuscación real, firma persistente, pruebas de pantallas y auditorías del APK instalado antes de fusionar.

La captura aportada es estática y su último slot todavía muestra el icono de espera; no documenta el instante de confirmación del décimo retrato. La regresión del retrato utiliza las capturas de Vi/Volibear existentes y verifica la aplicación segura de la decisión.

## Archivos

- `app/build.gradle.kts`
- `app/src/main/java/com/example/service/ConfirmedLastPickHud.kt`
- `app/src/main/java/com/example/service/FloatingAssistantService.kt`
- `app/src/main/java/com/example/service/screen/AdaptiveScreenLayoutEngine.kt`
- `app/src/main/java/com/example/service/screen/ChampionNameResolver.kt`
- `app/src/main/java/com/example/service/screen/DraftVisionScanner.kt`
- `app/src/main/java/com/example/service/screen/EnemyDraftReconciler.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/ui/screens/MetaAndDraftScreen.kt`
- `app/src/test/java/com/example/BuildCoachingRenderedTest.kt`
- `app/src/test/java/com/example/SpellCatalogRenderedTest.kt`
- `app/src/test/java/com/example/TenthPickRegressionTest.kt`
- `app/src/androidTest/java/com/example/DraftRivalNameInstalledTest.kt`
- `app/src/androidTest/assets/draft/`
- `tools/verify-draft-rival-names-device.py`
- `.github/workflows/build-apk.yml`
- Este reporte.
