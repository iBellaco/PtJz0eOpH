# Coach 1.1.10.384 — build 1100

La ejecución 40275756823 aprobó unitarias y falló el reconocimiento de VI en
los tres escenarios release. La alternativa neutra devolvió PV! en 1920×924.
La inspección del recorte neutral-4.png muestra un resto del emblema separado
de V e I. No se incorpora PV! como alias ni se declara resuelto el fallo.

La máscara del emblema agrupaba solo píxeles cálidos y dejaba su borde claro.
La conectividad ahora incluye píxeles claros neutros junto a los dorados; la
regla de tamaño del componente sigue diferenciando el emblema alto de letras.
Se elimina la absorción radial de componentes separados para preservar títulos.
Una regresión sintética verifica que desaparezca un ala clara conectada al
emblema y que permanezcan intactos dos trazos separados del título. Esta prueba
es de procesamiento de píxeles, no evidencia de reconocimiento de campeones.

Archivos: DraftImagePreprocessor.kt, DraftNameAtlasTest.kt, app/build.gradle.kts
y este informe. Se mantienen las capturas reales y la cobertura de 142 nombres
en fix-error. Pruebas Python 9/9 y diff limpio; pruebas Android pendientes de
Actions por falta de SDK local. Emulador/imágenes no equivalen a prueba física.
