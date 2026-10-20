# Auditoría Coach 1044

## Cambios

- La categoría interna `Defensa` se conserva para no romper filtros ni datos guardados, pero se muestra como `Objetos Defensivos` en el catálogo, las fichas y el panel de detalle.
- Los selectores de objetos y runas del panel de creadores ahora muestran chips de categoría con contador y tarjetas visuales con icono, nombre y coste.
- El filtrado por categoría respeta la disponibilidad actual de cada sección y mantiene las reglas de no duplicar objetos.

## Verificación

- Se incrementó `versionCode` a 1044 y `versionName` a `1.1.10.328`.
- La compilación y las pruebas de interfaz se ejecutan en el flujo oficial de GitHub Actions.
