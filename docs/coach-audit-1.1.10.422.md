# Coach 1.1.10.422 — build 1138

## Limpieza aplicada

Se eliminó por completo el componente de rastreo de enfriamientos en tiempo
real, incluido su estado compartido, temporizadores, selección de posiciones,
controles de inicio, ajuste, reinicio y notificaciones.

La búsqueda de referencias en la aplicación no encontró llamadas, botones ni
rutas adicionales hacia ese componente. Se añadió una comprobación que confirma
que el archivo, el estado y los marcadores de navegación no reaparezcan en la
interfaz flotante ni en pantallas auxiliares.

La información descriptiva de enfriamientos que pertenece al catálogo de
hechizos, objetos y habilidades se conserva; no es un rastreador activo.

## Verificación

- Comprobación de ausencia del componente y su estado.
- Comprobación de ausencia de navegación o controles del rastreador.
- Recursos localizados y formato del cambio revisados.
- La compilación Android completa queda para la ejecución automatizada.
