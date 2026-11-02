# Coach 1.1.10.451 — build 1167

La prueba insetColumnsAndHudSizesAreMeasuredWithoutModelCoordinates detectó una
regresión del seguimiento aliado: un contorno parcial a tamaño nominal evitaba
probar el tamaño real 1.2 y desplazaba el centro. Falló 1 de 257 pruebas.

DraftRingAlignment compara cobertura angular entre tamaños nominal, 0.8 y 1.2
para aliados. Conserva seguimiento individual, base del viewport y posiciones
anteriores sin evidencia. Una cobertura nominal completa evita búsquedas extra.
El ajuste rival permanece igual. DraftRingAlignmentTest conserva su tolerancia
de posición y añade la comprobación de diámetro en la matriz de dimensiones.

Se incrementa la versión en app/build.gradle.kts. Pruebas Android pendientes
de la nueva ejecución; no se afirma verificación física en Honor.
