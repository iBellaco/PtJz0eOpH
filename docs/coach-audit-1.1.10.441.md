# Coach 1.1.10.441 — build 1157

- Aislar FloatingWindowLifecycleTest con android.app.Application, como las pruebas de navegación del visor. El inicio de com.Coach activaba sincronización de producción innecesaria para probar ventanas y captura.
- Evidencia del fallo: ejecución 41316745815, detachedWindowHasItsOwnOwnersAndCanComposeSavedState, error asíncrono de persistencia con Illegal connection pointer en ShadowLegacySQLiteConnection. 29 pruebas ejecutadas en el paso, una fallida; compilación release y pruebas instaladas completadas correctamente.
- Se mantienen ambas pruebas y todas sus aserciones: captura inactiva, reanudación, pantalla apagada, liberación y ciclo de vida Compose. No se modifican las optimizaciones productivas de batería y arrastre.
- Archivos: FloatingWindowLifecycleTest.kt y app/build.gradle.kts.
- Verificación local: diff y recursos localizados. Reejecución Android pendiente de fix; el entorno local no dispone de la descarga de Gradle funcional. Sin nuevas afirmaciones de ahorro medido en teléfonos.
