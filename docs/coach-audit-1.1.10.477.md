# Coach 1.1.10.477 — build 1193

## Actualización obligatoria en producción

- Una versión de producción ahora trata cualquier lanzamiento publicado más
  reciente como obligatorio. El diálogo deja de mostrar «Más tarde» y bloquea
  la aplicación hasta que el usuario actualice.
- Las versiones de prueba con sufijo `-prueba.N` siguen fuera de la verificación
  oficial y no reciben este bloqueo.
- Se incrementa la versión a `1.1.10.477` (build 1193).

## Verificación

- La regresión de actualizaciones confirma que un lanzamiento nuevo sin código
  mínimo configurado también es obligatorio y no puede descartarse.
- Se mantiene la prueba de actualización obligatoria existente.
- La compilación Android completa queda pendiente de la ejecución de Actions.
