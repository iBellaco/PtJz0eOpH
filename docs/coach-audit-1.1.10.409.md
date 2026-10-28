# Coach 1.1.10.409 — build 1125

## Solicitud y diagnóstico

El tester POCO comunica un baneo Mel leído como Sion, un Darius sin reconocer,
primera selección rival mostrada sin evidencia y composiciones rivales con Nami
top o Tristana jungla. La captura identifica el síntoma, pero no está disponible
como archivo local de prueba ni incluye las dos composiciones completas narradas.
No se declara una reproducción física ni una comparación píxel a píxel de esa
captura. Los equipos completos de las pruebas son escenarios construidos y se
identifican como tales.

Se encontraron tres causas verificables en el código:

- El booleano inicial falso se representaba como primera selección rival, aunque
  el escáner todavía publicaba orden desconocido.
- La asignación rival favorecía posiciones ordinales y elecciones voraces. Además,
  FloatingDraftCoachView volvía a repartir por rol primario un equipo que ya
  estaba ordenado por líneas; esto deshacía la asignación y las correcciones manuales.
- El reconocimiento inicial de baneos comparaba solo ocho candidatos; las
  identidades aceptadas nunca volvían a refinarse ni podían corregirse. Tras diez
  confirmaciones se detenía por completo la lectura de la cabecera.

## Correcciones

- Estado explícito de orden observado: mostrar «por confirmar» hasta que existan
  títulos confirmados o elección manual. No contar el retrato provisional como
  evidencia de primera selección. Mantener la memoria de orden entre lecturas
  incompletas y el contrato de nueve títulos más décimo visual.
- EnemyRoleInference evalúa hasta 120 asignaciones del conjunto completo. No usa
  la fila de selección como línea. Prefiere roles compatibles y penaliza mucho
  una jungla sin compatibilidad; permite alternativas conservadoras de top para
  tiradores como Tristana, sin dar esa afinidad a todos los ADC.
- El Hub conserva las filas calculadas y manuales, incluidos huecos. Al cambiar
  el décimo rival vuelve a estimar las líneas sin cambiar las otras identidades.
  Identificar un retrato no significa certeza del 100% sobre su línea oculta.
- Los baneos comparan todas las identidades del catálogo en las posiciones
  candidatas; se refinan también coincidencias débiles. Una confirmación débil
  solo se sustituye tras dos observaciones coincidentes con similitud mínima
  0,90 y mejora mínima 0,05. Se preservan máscaras, posiciones vacías y márgenes.
  Un campeón seleccionado no puede permanecer como supuesto baneo. La cabecera
  completa se revisa cada 1500 ms en lugar de detenerse; continúa en el trabajador
  de fondo independiente del OCR de títulos. Sin coordenadas por fabricante.

## Catálogo y builds

Fuente oficial consultada el 28 de octubre de 2026:
https://wildrift.leagueoflegends.com/es-es/news/game-updates/wild-rift-patch-notes-7-3d/

Se contrastó también la publicación intermedia de cambios de objetos y runas:
https://wildrift.leagueoflegends.com/es-es/news/game-updates/wild-rift-patch-notes-7-3b/

- Dardos rastreademonios: 85% del daño crítico normal y 17,5% adicional verdadero
  bajo la condición de crítico previo. Se corrige el consejo que prometía daño
  verdadero continuo.
- Desgarrador divino: 3300 de oro.
- Segador de esencia: 55 de daño de ataque.
- Ambas diademas: 6% de poder de curaciones y escudos.
- Navaja de asalto: 100 de daño y 35% de movimiento; se retira el consejo antiguo
  que decía que ralentizaba al enemigo.
- Los valores de Electrocutar, Aery, Garras del Inmortal y Golpe Bajo ya reflejaban
  los cambios anteriores. La publicación nueva no anuncia cambios de runas ni
  hechizos: no se inventan modificaciones de cifras o enfriamientos.
- Sin etiquetas de versión en las fichas, recomendaciones y consejos actualizados.
  La versión interna para invalidación de caché queda actualizada. Las versiones
  históricas de fuentes externas se conservan: no se finge una descarga nueva.
  El validador comprueba que esa evidencia tenga formato válido y no sea futura.
- Miss Fortune ADC pasa a Segador de esencia, Recaudadora y Filo Infinito como
  base táctica; su antigua opción de movilidad sigue siendo situacional. Soporte
  conserva su objeto de ingresos. Se añaden alternativas condicionadas de
  Desgarrador para Camille/Nasus/Jax/Wukong y Dardos para Ashe/Tristana/Xayah ADC.
  No se atribuyen estas propuestas a estadísticas ni porcentajes de victorias
  nuevos. Se regeneran las 300 builds de 142 campeones con consejos bilingües.

## Archivos modificados

- app/build.gradle.kts
- .github/workflows/fix-error.yml
- .github/scripts/validate-patch-content.py y test_patch_content.py
- app/src/main/java/com/example/service/OverlayState.kt, ConfirmedLastPickHud.kt,
  FloatingOverlayContent.kt y FloatingDraftCoachView.kt
- app/src/main/java/com/example/service/screen/EnemyRoleInference.kt,
  EnemyDraftReconciler.kt, DraftValidationLayer.kt, DraftVisionScanner.kt,
  BanDetection.kt, BannedPortraitScanner.kt y LiteRTVisionClassifier.kt
- app/src/main/java/com/example/data/GamePatch.kt, WildRiftItemsData.kt y
  SituationalItemAdvisor.kt
- app/src/main/java/com/example/util/BuildElementAdvice.kt
- app/src/main/res/values/draft_order.xml y equivalentes values-pt/values-pt-rBR
- app/src/main/res/raw/champions_part1.json y champions_part2.json
- app/src/main/assets/champions_creator_builds.json, translations_pt.json y runes.json
- tools/build_coaching.py y tools/update-champion-builds.py
- app/src/test/java/com/example/EnemyRoleInferenceTest.kt, DraftOrderUiTest.kt,
  CatalogBalanceTest.kt, BannedPortraitScannerTest.kt y DraftAutoStopTest.kt
- app/src/test/resources/item-corrections-158.json
- docs/draft-recognition-contract.md y este reporte

## Verificaciones

Pasaron localmente todas las comprobaciones previas de catálogo usadas por el
flujo de validación: recursos, contenido, planes OCR, informes, generadores e
idioma. La auditoría de interfaz portuguesa evaluó 2617 apariciones de literales
sin hallazgos de español. Los tres casos del validador de versiones pasaron.
git diff --check y análisis YAML sin errores.

Se compiló y ejecutó una comprobación JVM de los archivos Kotlin productivos de
modelo y políticas con el catálogo real: 240 permutaciones de los dos escenarios,
conservación de filas/huecos y orden de selección. Usa sustitutos únicamente para
tipos de presentación/recursos/anotación/traducción ajenos a esas políticas; no
equivale a ejecutar la aplicación Android. Esta comprobación detectó y permitió
corregir el empate que mandaba Jinx a top en lugar de Tristana.

También pasaron los tres casos de CatalogBalanceTest con JUnit real y los
catálogos Kotlin productivos de objetos, componentes, runas y hechizos. En esa
ejecución aislada se sustituyeron los tipos de presentación y el acceso al
repositorio de runas, no las cifras ni las listas del catálogo. No sustituye el
renderizado Android ES/PT/ES pendiente en Actions.

Pruebas Android añadidas al flujo fix-error:

- Los 240 órdenes de composición, locks manuales, renderizado de filas y cambio
  del décimo rival; orden desconocido y botón manual en interfaz ES/PT.
- Corrección temporal Sion → Mel con evidencia mejor, invalidación de un falso
  baneo al seleccionar ese campeón, cabecera sintética Mel/Darius a tres escalas
  y todos los fixtures reales anteriores de baneos.
- Los seis valores nuevos, preservación de runas/hechizos, catálogos completos,
  renderizado ES/PT/ES de los objetos y coherencia de builds.
- Se mantienen las regresiones anteriores de cierre del draft y patrocinio.

El entorno local carece de Android SDK. El intento inicial del wrapper tampoco
pudo descargar Gradle con la configuración Java heredada. La compilación Android,
las pruebas de imagen/interfaz y R8 quedan pendientes del flujo autorizado; no se
declaran realizadas ni se entrega un APK nuevo sin validar. No hay prueba física
de esta versión en POCO ni otros dispositivos. Falta medir latencia/batería real
del refinamiento adicional de cabecera.

## Entrega

Solo fix-error. Esperar el resultado comunicado por el propietario antes de
promover a coach-validacion. No fusionar main ni mantener monitoreo continuo.

Resumen para testers:
Coach 1.1.10.409: revisar baneos Mel/Darius, primera selección sin datos,
Yone top con Yi jungla, Tristana top con Pantheon jungla, cambios del último
campeón y elecciones manuales. Catálogo y builds revisados en español y portugués.
Las líneas rivales continúan siendo estimaciones.

Commit: Corregir baneos, orden y roles del draft y actualizar catálogo y builds
