# Coach 1.1.10.592 (1308) · Tema de aplicación Custom

Coach incorpora un tema de aplicación `Custom` al final del carrusel de temas.
La personalización queda separada en cuatro paletas: Primario, Secundario,
Resplandor y Suplente. Cada sección mantiene su propia selección y permite
repetir un mismo color en varias secciones.

## Cambios

- Se añadió `Custom` como el último tema de aplicación.
- Se incorporaron paletas independientes con 20 colores y estados visibles de
  selección para cada sección.
- Una instalación sin una personalización guardada muestra las cuatro secciones
  vacías y el botón `Aplicar` deshabilitado.
- El botón `Aplicar` solo se habilita después de completar los cuatro colores.
- La selección completa se conserva localmente y se restaura al volver a abrir
  el panel de temas.
- Los colores elegidos alimentan la paleta principal, secundaria, de resplandor
  y alternativa del tema aplicado.
- Se conservaron los temas existentes y se actualizaron las comprobaciones del
  catálogo y de la superficie renderizada en portugués.

## Verificaciones

- `:app:compileDebugKotlin` pasó.
- `ApplicationThemeCatalogTest` y `LocalizationRegressionTest` pasaron.
- La prueba de interfaz de `Custom` confirmó que `Aplicar` permanece bloqueado
  hasta completar las cuatro paletas y se habilita después.
- `PortugueseRenderedAuditTest` pasó para las superficies renderizadas.
- La compilación release ofuscada y la comprobación de firma persistente quedan
  a cargo de la validación de producción configurada para esta versión.
