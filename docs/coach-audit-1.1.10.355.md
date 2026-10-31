# Coach 1.1.10.355 — build 1071

La validación completa 40155186022 todavía ejecuta las cuatro particiones
OCR nativas, pero `installed-spanish` ya falló con dos aserciones del APK
release: la captura Red Magic 7448 devolvió cero aliados en el modo rápido
en vez de Senna, Fizz, Nami y Malphite; otra captura no confirmó a Malphite
en el slot aliado 3. Las otras pruebas del job y el gate OCR preliminar sí
pasaron. El fallo impide aprobar/publicar esta revisión.

Para aislar la causa, `fix-error` añade un job paralelo que compila el mismo
tipo release ofuscado con una clave efímera de prueba y ejecuta únicamente
esas dos pruebas instaladas. Las aserciones ahora incluyen slots, rivales,
líneas observadas y estado del escáner, de modo que un nuevo fallo indique
qué lectura se perdió. Se conserva el job OCR P20/Honor que ya estaba verde.

Archivos modificados: `.github/workflows/fix-error.yml`,
`app/src/androidTest/java/com/example/DraftRivalNameInstalledTest.kt`,
`app/build.gradle.kts` y este informe. Esta revisión añade diagnóstico, no
afirma haber corregido aún el reconocimiento ni haber probado un teléfono
físico. El flujo aislado debe ejecutarse antes de trasladar otra corrección
a `coach-validacion`.
