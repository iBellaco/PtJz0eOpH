# Coach 1.1.10.575 · Calibración OPPO y filas manuales de baneos

## Cambios

- Se añadió el perfil exacto del OPPO CPH2791 para la pantalla física y sus dos
  alias de captura reportados.
- Las filas aliada y rival de baneos de ese perfil permanecen en coordenadas de
  captura manuales y no se redistribuyen junto con las columnas de selección.
- Se conservaron como respaldo las referencias textuales de Xiaomi y OPPO.
- Las imágenes originales adjuntas en la conversación no estaban disponibles
  como archivos locales; no se generó una imagen sustituta.

## Pruebas

- Se añadieron comprobaciones de alcance por fabricante, modelo y dimensiones.
- Se añadió una comprobación de que las filas manuales no se transforman al
  calcular la geometría de los slots.
- La suite local no pudo iniciar porque la distribución de Gradle no estaba
  disponible y el entorno no pudo descargarla; la validación remota de la rama
  queda como comprobación de compilación y pruebas.
