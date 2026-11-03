# Coach 1.1.10.469 — build 1185

## Estabilidad de validaciones instaladas

- Las auditorías instaladas extensas usan el renderizador de software estable
  en lugar del modo indirecto que perdió búferes de color en dos ejecuciones.
- La revisión visual de idioma se ejecuta antes de encadenar instrumentaciones
  que reinician o reinstalan la aplicación.
- La prueba instalada de nombres vuelve a instalar explícitamente el APK y su
  instrumentación, detiene procesos anteriores y concede el permiso requerido
  antes de comenzar.

## Verificación

- Regresión del orden y renderizador de las dos auditorías instaladas.
- Regresión del estado limpio previo a la instrumentación de nombres.
- Las comprobaciones funcionales de patrocinio, seguridad y compilación de la
  versión anterior ya habían terminado correctamente; el fallo corregido estaba
  limitado al emulador de las auditorías instaladas.
