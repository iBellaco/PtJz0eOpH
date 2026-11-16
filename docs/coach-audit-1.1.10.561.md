# Coach 1.1.10.561 — selector regional animado y scraping tolerante a fallos

Versión: 1.1.10.561; versionCode: 1277.

## Cambios

- La selección de servidor Global/China tiene una transición visual de color y
  escala, conserva un área táctil mínima de 48 dp y mantiene sus identificadores
  de prueba.
- Se retiró el encabezado visible «Segmento China por Elo» y su texto
  redundante; los cuatro rangos publicados siguen disponibles en el selector.
- China ya no fuerza la vista por Tier: Win Rate y Ban Rate ordenan las filas
  usando las estadísticas de la línea y del rango China seleccionado, con un
  desempate estable por nombre.
- El scraping global separa la página de categorías de la página de tasas. Si
  una de ellas falla temporalmente, intenta la otra y valida el parche antes de
  aceptar los datos. Una respuesta secundaria no puede borrar una lectura
  válida de la primera.
- El lector de WildRiftFire acepta tasas porcentuales y fracciones decimales
  por fila, y usa el nombre del campeón como respaldo cuando falta el slug.
  Las filas sin las tres tasas completas continúan fuera de la tier list.

## Integridad de datos

- No se inventan tasas para campeones que la fuente no publica en el parche
  actual. El monitoreo conserva sus identificadores como ausentes para que la
  diferencia se distinga de un fallo de conexión o de interpretación.
- El flujo China continúa separado de Global y mantiene cada rango como una
  instantánea independiente.

## Verificación

- `git diff --check` correcto.
- Suite dirigida correcta con JDK 21: `GlobalTierScrapingTest`,
  `GlobalTierSourceClientTest` y `TierListSortingTest`.
- Las pruebas cubren la recuperación de tasas cuando la página de tier falla,
  la conservación de datos cuando falla la página secundaria, tasas decimales
  de WildRiftFire y ordenamiento China por Win Rate y Ban Rate.
- La compilación release local quedó pendiente porque este entorno no contiene
  la clave de producción; la compilación oficial debe validar R8, firma y el
  APK final con la configuración protegida.
