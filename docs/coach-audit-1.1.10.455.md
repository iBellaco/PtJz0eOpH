# Coach 1.1.10.455 — build 1171

La ejecución 41458381141 falló en una de 257 pruebas: con solo las filas 0 y 1
visibles, la fila 2 invisible pasó de X=0.072 a X=0.0890625. El retorno anticipado
para columnas coherentes asignaba la mediana a las cinco filas y declaraba todas
medidas, incluyendo las ocultas. También podía cambiar sus diámetros.

## Cambios

- `DraftRingAlignment.kt`: eliminar ese retorno anticipado; aplicar la geometría
  común únicamente a las filas observadas. Conservar X, Y y diámetro anteriores
  de las filas sin evidencia, en la misma ruta que maneja columnas mixtas.
- `DraftRingAlignmentTest.kt`: repetir los casos visibles/ocultos con valores
  iniciales y con geometría previa diferente. Exigir conservación exacta de X,
  Y y diámetro ocultos, manteniendo las tolerancias de localización visibles.
- `app/build.gradle.kts`: versión 1.1.10.455 y build 1171.

## Verificación y límites

Pasan 75 pruebas de scripts, la validación de recursos de idiomas y
`git diff --check`.

La matriz de centros y baneos no llegó a ejecutarse en el último intento porque
falló el paso anterior. Su error previo sigue pendiente de verificación; este
cambio no demuestra que se haya resuelto. No se modifican umbrales ni se retiran
pruebas. Las pruebas Android necesitan la ejecución remota; no se afirma que
esta modificación esté validada en teléfonos físicos.
