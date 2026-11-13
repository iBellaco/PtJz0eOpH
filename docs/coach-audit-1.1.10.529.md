# Auditoría Coach 1.1.10.529

- `versionCode`: `1245`.
- `versionName`: `1.1.10.529`.

## Informe de coordenadas y evidencia visual

- Los informes extensos llegan contraídos y se expanden desde el control de la tarjeta, el título o el contenido cuando se necesita revisar el texto completo.
- Se retiró la banda de texto que se dibujaba en la esquina superior izquierda de la evidencia y cubría la fila de baneos.
- La evidencia usa las regiones de baneos detectadas cuando están completas; si la lectura es parcial conserva la calibración guardada para no desplazar los huecos.
- La captura de respaldo puede incluir los nombres de campeones confirmados por puesto, sin convertir líneas u ordinales en identidades inventadas.

## Visor de selección

- Las cinco etiquetas aliadas y rivales comparten la misma posición relativa sobre su fila.
- El primer slot ya no usa una etiqueta lateral especial cuando la detección de baneos está activa.
- Al existir una identidad confirmada, se muestra el nombre del campeón; si no hay confirmación se conserva el marcador del slot.
- Se mantienen las coordenadas de calibración y la transformación por viewport, sin sustituir perfiles específicos por valores de otra resolución.

## Verificaciones realizadas

- Revisión de diferencias con `git diff --check`.
- Validación de sintaxis de los archivos Kotlin y recursos modificados mediante las comprobaciones del proyecto.
- La compilación Android y las pruebas instrumentadas quedan para la ejecución de validación con el SDK configurado.
