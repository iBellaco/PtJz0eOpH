# Coach 1.1.10.385 — build 1101

La ejecución 40279619782 confirmó los nueve títulos reales y Teemo en
1920×924 y 1920×889. Sigue fallando VI en 1920×822. Estas son pruebas de
capturas redimensionadas instaladas en emulador, no teléfonos físicos.

También falló alliedBadgeMarginCanBeRemovedWithoutChangingTheTitleSource:
un cuadro blanco uniforme fue clasificado como emblema tras ampliar la máscara
a tonos neutros. Es una regresión real, no una expectativa obsoleta. Se mantiene
el assert original y se exige ahora un núcleo cálido en el componente. El
fallback de columnas también mide exclusivamente los píxeles cálidos.

El recorte neutral-4.png de 1920×822 muestra un residuo pequeño separado antes
de V e I. Solo en la alternativa neutra se eliminan componentes simultáneamente
menores al 60 % de la altura máxima y al 15 % del área máxima. Una prueba
sintética exige eliminar un residuo y conservar un trazo alto estrecho como I.
Se añade una repetición de los píxeles neutros limpios; no se generan nombres ni
se amplían alias. Se conservan las variantes anteriores y las dos confirmaciones.

Archivos: DraftImagePreprocessor.kt, DraftNameAtlas.kt, DraftVisionScanner.kt,
DraftNameAtlasTest.kt, app/build.gradle.kts y este informe. Se mantienen los tres
escenarios y la cobertura obligatoria de 142 nombres. Verificación local: 9/9
pruebas Python y diff limpio. Kotlin/ML Kit pendientes de Actions por falta de
SDK local. No se modifica main ni coach-validacion.
