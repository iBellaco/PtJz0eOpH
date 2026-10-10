# Coach 1.1.10.244 — código 960

## Cambios

- La tier list muestra exclusivamente campeones propios identificados en las partidas guardadas. Se elimina la inclusión del catálogo sin partidas y la selección arbitraria del primer aliado o rival cuando falta el slot de línea. Los registros no identificados siguen guardados y se indica cuántos no entran en el análisis.
- Menos de cinco resultados finalizados: clasificación provisional. La ordenación suaviza muestras pequeñas con cuatro resultados neutrales; no se concede una clasificación S+ por una sola victoria. La línea destacada y los rivales destacados requieren cinco resultados. Cinco resultados es un umbral de presentación, no una garantía estadística.
- El campeón más jugado se elige por volumen registrado. El historial de rivales describe resultados de partidas completas, sin atribuir dominio de duelos, prioridades de oleada o errores de microjuego que no se registraron.
- El KDA opcional muestra promedio de ratios por partida y cobertura de resultados informados. Vacíos, notas inválidas y partidas pendientes no se consideran cero; cero muertes utiliza divisor uno. Se conserva compatibilidad estricta con ratios de registros antiguos. El KDA aporta preguntas de revisión y no infla la clasificación.
- Durante el draft, el historial ante un rival corresponde al perfil activo, campeón propio y línea, sin mezclar otros campeones o cuentas.
- Guardar una partida no identifica campeones a partir de slots vacíos o roles duplicados. Actualizar un draft existente también actualiza la identidad guardada.
- Textos nuevos en español y portugués; sin cambios de esquema ni pérdida del historial.
- La revisión de capturas detectó que el título comprimía el selector Detallado. Se reserva ancho para ambos botones y se añade una regresión de texto de una sola línea en ambos idiomas.
- La auditoría de pantallas ahora desmonta los listeners de la composición antes de cerrar la conexión de prueba y permite procesar callbacks del hilo principal mientras espera el cierre. Se mantiene un timeout real y no se ignoran errores de la tarea.

## Archivos

- `PersonalTierListManager.kt`, `PersonalTierListView.kt`, `DraftAnalysisTab.kt`.
- `DraftHistoryRepository.kt`, `translations_ui_pt.json`.
- `PersonalTierListRegressionTest.kt`, `HistoryConsistencyRenderedTest.kt`, `PortugueseRenderedAuditTest.kt`.
- `app/build.gradle.kts`, esta auditoría.

## Verificación

- Once pruebas locales del cálculo aprobadas con el gestor y entidad reales, sustituyendo dependencias Android externas por dobles mínimos para el compilador Kotlin. Actions comprobará también las dependencias reales.
- Catálogos generados, iconos y formato de diferencias comprobados. Auditoría de portugués: 2.593 textos visibles, sin residuos españoles.
- La ejecución `38015247180` aprobó lint, 560 pruebas sin fallos ni omisiones y la revisión de pantallas español/portugués. Sus capturas motivaron el ajuste de ancho del selector; la última revisión debe repetir la validación Android y completar APK release ofuscado, firma persistente y validación instalada.
- La ejecución `38016599971` volvió a aprobar las 560 pruebas y el selector corregido, pero falló el cierre de la conexión en una auditoría de tema Premium durante la segunda pasada. Se corrigió el ciclo de cierre del test; sus capturas muestran el selector sin cortes en ambos idiomas. La entrega espera las comprobaciones obligatorias de la revisión final.
- El despliegue previo del servicio directo continúa bloqueado externamente por la habilitación o acceso a Cloud Build; esta intervención no oculta ese fallo.

## Resumen para testers

Coach 1.1.10.244: verificar que la tier list solo incluya campeones propios registrados, que una sola victoria no otorgue una calificación definitiva y que los rivales correspondan a la línea guardada. Comprobar el promedio y la cobertura del KDA opcional; dejarlo vacío no debe contar como cero. Cambiar de perfil, línea y cola y revisar que las estadísticas correspondan al filtro. Repetir en español y portugués.
