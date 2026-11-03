# Coach 1.1.10.466 — build 1182

## Actividad de usuarios

- Cada categoría ordena sus usuarios por la última conexión registrada, de la
  más reciente a la más antigua.
- La categoría de usuarios conectados también conserva ese orden.
- Se mantiene la indicación de conexión actual o de última conexión.
- El correo electrónico deja de mostrarse en la lista de actividad.
- La información completa de gestión sigue separada de esta vista resumida.

## Verificación

- Se añadió una regresión para comprobar el orden por última conexión.
- Se canceló la ejecución anterior de corrección antes de integrar esta
  modificación en el mismo flujo.
