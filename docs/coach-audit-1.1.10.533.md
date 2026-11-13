# Auditoría Coach 1.1.10.533

- `versionCode`: `1249`.
- `versionName`: `1.1.10.533`.

## Corrección del scraping de estadísticas

- WildRiftCore puede ocultar las métricas en las etiquetas visuales de una
  tarjeta aunque las publique en el JSON `tlRankData` de la misma página.
- El extractor ahora lee ese conjunto de datos para recuperar win rate,
  selección y baneos por campeón y por línea, sin sustituir valores globales
  explícitos del panel ALL.
- Cuando la tarjeta solo expone una frase verificable de parche actual con
  win rate y pick rate, esos dos valores también se muestran y el ban rate se
  mantiene como no publicado.
- No se generan estadísticas para tarjetas que solo tienen categoría o una
  explicación sin métricas.

## Verificación

- Se añadió una prueba de regresión para el JSON incrustado y la frase de
  métricas de la tarjeta.
- La suite local no pudo iniciar porque este entorno no tiene configurado el
  SDK de Android (`local.properties`/`ANDROID_HOME`); la validación selectiva
  debe ejecutarse en el entorno de CI con el SDK disponible.
