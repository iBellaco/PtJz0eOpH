# Coach 1.1.10.454 — build 1170

## Error reproducido

La prueba dirigida de capturas con paneles laterales seguía fallando en
resoluciones pequeñas. Cuando todos los anillos aliados detectados formaban una
columna coherente, el ajuste por fila podía escoger contornos parciales y
desplazar los recortes; la misma captura también podía dejar entrar una
propuesta de baneo vecina.

## Modificación

`DraftRingAlignment` vuelve a usar el ajuste robusto de columna cuando todas
las observaciones visibles son coherentes. El ajuste por fila se conserva para
el caso que motivó la corrección: filas aliadas realmente separadas o faltantes.
Así se mantienen ambas secuencias sin coordenadas por fabricante y sin acumular
escalas entre fotogramas.

La versión se incrementó y la prueba dirigida continúa incluyendo los casos de
centros desplazados y la captura reducida de baneos.

## Verificación

Se requiere la ejecución Android remota para completar la comprobación; el
entorno local no puede descargar Gradle. La prueba debe aprobar las capturas
normales, cuadradas, con paneles laterales y las secuencias de reconocimiento.
