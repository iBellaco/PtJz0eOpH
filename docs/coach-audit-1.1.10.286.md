# Auditoría de cambios Coach 1.1.10.286

## Actividad de usuarios

- Cada rol muestra su total en blanco y su cantidad conectada debajo.
- La cantidad conectada usa verde cuando es mayor que cero y gris cuando es cero.
- Se retiraron las filas redundantes de sesión y sin sesión.
- Se añadió el rol gratuito junto a administradores, moderadores, patrocinadores, streamers y creadores.
- La gestión de usuarios ahora permite editar y guardar el nombre público, sincronizando los campos usados por la aplicación.

## Tier list

- La gráfica tiene un pulso continuo y conserva verde, rojo o cian según la variación.
- Se quitó la etiqueta repetida «Tendencia» dentro de cada gráfica; el trazado ocupa ese espacio.
- Se añadió una descarga PNG con los campeones visibles de S+ a A, avatar visual, nombre, win/pick/ban, logo y derechos.

## Versión y validación

- `versionCode`: 1002; `versionName`: 1.1.10.286.
- Se añadieron las traducciones portuguesas de las nuevas etiquetas.
- Compilación, pruebas de datos, renderizado, instalación y auditoría de idioma se ejecutan en GitHub Actions.
