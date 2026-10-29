# Coach 1.1.10.411 — build 1127

## Fallos comprobados

La validación 40748016919 de 1.1.10.410 falló en dos trabajos:

- scanner-tests (123027515702): tres casos de PortraitMatcherTest llegaron a
  reconocer Vi correctamente, pero fallaron al comprobar su posición en el Hub
  (línea 126). Los objetos de prueba solo tenían identificador: todos heredaban
  MID como rol predeterminado. Ya no representan una composición válida para
  la estimación global de roles rivales añadida en 1.1.10.409.
- core-and-rendered (123028274149): documentedRequestedBrandDisplays confundió
  Jhin con Caitlyn en la variante con retratos y baneos cambiados del perfil
  854 × 480. Es una captura de referencia redimensionada, no una prueba física
  en Unihertz.

Catálogos, lint y los trabajos de permisos y servicios terminaron correctamente.
No se generó un APK validado en esa ejecución.

## Causa y corrección

La reproducción local del segundo fallo reconoció inicialmente Jhin (0,839807),
pero el refinamiento de recortes lo sustituyó por Caitlyn (0,851407). Una mejora
de 0,0116 no justifica cambiar de identidad. Ahora el refinamiento utiliza la
misma regla de sustitución que el seguimiento temporal: mínimo 0,90 y mejora
mínima 0,05. Mejorar el encuadre de la misma identidad sigue permitido. Antes
de aceptar un recorte refinado se compara contra todo el catálogo, manteniendo
el umbral y la separación entre candidatos. La confirmación del baneo sigue
requiriendo dos fotogramas; el refinamiento no constituye un fotograma extra.

PortraitMatcherTest carga el catálogo real y usa sus campeones con roles para
formar los nueve picks. Se conservan todas las aserciones: Vi en jungla, nueve
identidades previas en sus posiciones, bloqueo manual, dos fotogramas, diez
identidades distintas y recuperación tras desaparecer el retrato. No se altera
el algoritmo de roles ni el reconocimiento del décimo pick para satisfacer el test.

Se añade al flujo fix-error la clase PortraitMatcherTest y una regresión enfocada
del perfil 854 × 480 con las tres variantes de fondo/retratos. La matriz completa
de validación permanece intacta. Los informes de las dos invocaciones de Gradle
se guardan en artefactos separados para que la segunda no sobrescriba los primeros.

## Archivos modificados

- app/src/main/java/com/example/service/screen/BanDetection.kt: política común
  para sustituir identidades solo con evidencia claramente superior.
- app/src/main/java/com/example/service/screen/BannedPortraitScanner.kt: reutiliza
  la política sin cambiar la exigencia temporal.
- app/src/main/java/com/example/service/screen/LiteRTVisionClassifier.kt:
  protege la identidad aceptada durante el refinamiento y verifica competidores.
- app/src/test/java/com/example/BannedPortraitScannerTest.kt: límites de mejora.
- app/src/test/java/com/example/PortraitMatcherTest.kt: catálogo y roles reales.
- app/src/test/java/com/example/DraftMultiDeviceAuditTest.kt: regresión enfocada.
- .github/workflows/fix-error.yml: ejecutar y conservar ambos casos antes de validar.
- app/build.gradle.kts: versión y build.
- docs/coach-audit-1.1.10.411.md: este reporte.

## Verificaciones y límites

- Antes del ajuste, el adaptador local reprodujo Jhin → Caitlyn; después conserva
  Jhin y las diez identidades de la fila permutada en anchos 640, 800, 854, 1280 y
  1920. Utiliza el fixture real como fuente, composición sintética y las funciones
  productivas de cabecera con un adaptador gráfico de escritorio, no Android.
- El mismo adaptador conserva los diez baneos del fixture poco-header.jpg,
  incluidos Lee Sin y Rammus, y la transición de fila parcial a completa.
- La política Kotlin productiva de roles, compilada con el catálogo real,
  aprobó 360 órdenes de selección: Yone/Yi/Mel/Jinx/Nami,
  Tristana/Pantheon/Ahri/Jinx/Nami y Darius/Vi/Mel/Caitlyn/Milio. También pasaron
  conservación de filas del Hub y evidencia de primera selección. Usa adaptadores
  mínimos de dependencias; no sustituye las pruebas de interfaz Android.
- Validaciones de recursos ES/PT, contenido y builds generadas: correctas.
- git diff --check: correcto.

No hay SDK Android local disponible. La ejecución nativa de Robolectric y la
compilación Android quedan en Actions. No se afirma un verde nuevo ni se entrega
un APK no validado. Se conservan los arreglos de patrocinio y del último pick.

## Entrega

Solo fix-error; coach-validacion y main no se modifican. Esperar el aviso del
propietario para comprobar el resultado, sin monitoreo continuo.

Resumen para testers: Coach 1.1.10.411 corrige la sustitución incorrecta de Jhin
por Caitlyn en baneos de capturas pequeñas y refuerza las pruebas de Vi como
última selección. Pendiente de validación Android y comprobación física.

Commit: Evitar cambios de baneo por mejoras mínimas y probar Vi con roles reales
