# Coach 1.1.10.246 — código 962

## Cambios

- Al abrir una runa situacional, el detalle ahora muestra primero la condición de activación propia de esa runa.
- La recomendación específica de la build se conserva como una segunda sección y se eliminan fragmentos que repitan la descripción mecánica del catálogo.
- Se amplían las pruebas para verificar la distinción entre runas situacionales y la conservación del consejo de cada build en español y portugués.

## Archivos

- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/util/BuildElementAdvice.kt`
- `app/src/test/java/com/example/BuildElementAdviceTest.kt`
- `app/src/test/java/com/example/BuildElementAdviceCoverageTest.kt`
- `app/build.gradle.kts`
- `docs/coach-audit-1.1.10.246.md`

## Verificación

- Auditoría del catálogo: 300 builds oficiales, 14 registros de runas situacionales y 2 opciones distintas; no hay dos runas situacionales idénticas dentro de una misma build.
- Pruebas locales pendientes: este entorno no pudo descargar Gradle por rechazo de conexión. La validación de pruebas, compilación R8, firma e instalación se ejecutará en GitHub Actions antes de fusionar.

## Resumen para testers

Coach 1.1.10.246: abre builds con runas situacionales y toca cada runa. Comprueba que el efecto y la condición para elegirla correspondan a la runa seleccionada, que la nota específica de la build siga visible y que al abrir otra runa cambie el consejo. Repite en español y portugués.
