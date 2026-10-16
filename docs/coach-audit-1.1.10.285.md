# Auditoría de cambios Coach 1.1.10.285

## Fuentes

- Se retiraron BestBuildWR y WildRifter del agregador de estadísticas.
- El monitoreo conserva visibles únicamente las fuentes activas restantes y sus incidencias de cobertura o sincronización.

## Estadísticas y tier list

- La ficha del campeón fuerza una lectura del snapshot cada segundo para reflejar una sincronización terminada sin cerrar la vista.
- La gráfica usa verde cuando la comparación sube, rojo cuando baja y cian cuando se mantiene.
- El movimiento de tier se representa solo con un icono de subida o bajada, sin añadir texto a cada campeón.
- Los valores numéricos no se fabrican: cambian cuando una fuente válida publica una nueva medición.

## Métricas de usuarios

- Debajo del monitoreo se añadió un resumen en vivo de usuarios registrados, con sesión, sin sesión reciente y conectados.
- También se muestran totales y conectados de administradores, moderadores, patrocinadores, streamers y creadores.

## Validación

- Se incrementó `versionCode` de 1000 a 1001 y `versionName` a `1.1.10.285`.
- La compilación, las pruebas Android, las pantallas renderizadas y la auditoría de idioma se ejecutan en GitHub Actions.
