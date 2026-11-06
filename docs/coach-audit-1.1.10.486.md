# Coach 1.1.10.486 — Corrección de compilación de la animación de ahorro

## Corrección aplicada

- Se añadió la importación de `sp` que faltaba en `SubscriptionPlansBottomSheet`.
- La corrección no cambia el comportamiento de los cinco planes Premium ni la
  animación de sus ahorros; únicamente permite compilar el componente que los
  muestra.
- Se incrementó la versión de producción a `1.1.10.486` (build `1202`).

## Evidencia del fallo corregido

- La ejecución anterior pasó las comprobaciones de catálogos, idiomas y
  economía, pero `compileReleaseKotlin` falló con `Unresolved reference 'sp'`
  en `SubscriptionPlansBottomSheet.kt:144`.
- La misma compilación impidió iniciar las regresiones instaladas del Hub.

## Verificación local

- `git diff --check` y `node --check` de las funciones de economía quedan
  preparados para la nueva validación.
- El SDK Android no está instalado en este entorno, por lo que la compilación
  Android se comprobará en la ejecución remota de producción.
