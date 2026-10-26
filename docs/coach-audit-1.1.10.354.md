# Coach 1.1.10.354 — build 1070

La ejecución completa 40153365946 quedó roja en dos jobs por la misma prueba
`AllyDraftNameReaderTest.shortNamesStillRequireAnExactTitleInsideTheTitleRow`:
`expected:<vi> but was:<null>` en la línea 50. El título completo `FVI`
(glifo de insignia OCR + `VI`) no se reconocía porque la eliminación de un
prefijo suelto exigía longitud mayor que tres. La compilación release y sus
pruebas instaladas quedaron bloqueadas, no fallaron por sí mismas.

Se admite únicamente la lectura exacta `FVI` para `Vi`, sin habilitar la
eliminación general de prefijos de títulos de dos letras. La prueba agrega
rechazos para `XVI` y `VI7`, y el flujo aislado `fix-error` ejecuta ahora la
clase de pruebas que falló antes de compilar y lanzar OCR nativo. Así un verde
del flujo aislado también comprueba el error que detuvo la validación completa.

Archivos modificados: `app/build.gradle.kts`,
`app/src/main/java/com/example/service/screen/ChampionNameResolver.kt`,
`app/src/test/java/com/example/AllyDraftNameReaderTest.kt`,
`.github/workflows/fix-error.yml` y este informe. El resultado del flujo
aislado de esta revisión sigue pendiente; no equivale a una prueba física.
