# Coach 1.1.10.608 · Corrección de paleta neutra VOLT

## Corrección

- Se corrigió el blanco de texto del tema VOLT para que sus tres canales de
  color sean iguales y la paleta de aplicación permanezca completamente
  acromática.
- Se incrementó la versión visible de Coach a `1.1.10.608` (1324).

## Verificación

- El fallo aislado de la validación estaba en `ApplicationThemeCatalogTest`:
  VOLT usaba `#FFFEFE` en lugar de un blanco neutro.
- Se volverá a ejecutar la comprobación del artefacto de pruebas afectado
  dentro de la misma cadena de validación.
