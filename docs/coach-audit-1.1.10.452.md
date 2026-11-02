# Coach 1.1.10.452 — build 1168

La alineación aliada conserva ahora el ajuste robusto de columna cuando los
anillos visibles siguen compartiendo una misma posición horizontal. Solo usa
el seguimiento independiente cuando la evidencia demuestra que las filas se
han separado. Esto evita que un borde parcial o una imagen redimensionada
desplace una fila durante el reconocimiento, sin perder la adaptación a
layouts donde cada puesto cambia de posición.

La selección de escala, el viewport observado y la estabilidad entre cuadros
se mantienen multidispositivo y sin coordenadas asociadas a un modelo físico.
La matriz de regresión debe confirmar el caso de centros, las resoluciones
cuadradas y los paneles laterales antes de continuar con la validación.
