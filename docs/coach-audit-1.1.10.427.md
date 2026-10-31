# Auditoría Coach 1.1.10.427

## Corrección

- Se corrigieron los fixtures de visibilidad para que las pantallas administrativas de cambios de rol y retiro de Premium incluyan el claim administrativo firmado que exige la aplicación.
- Se evita que los controles aparezcan deshabilitados en la prueba y que el callback de resultado se invoque sobre una referencia nula.

## Verificación

- `versionCode`: 1143.
- `versionName`: `1.1.10.427`.
- La corrección reproduce las condiciones de autorización reales sin relajar la validación de permisos de producción.
