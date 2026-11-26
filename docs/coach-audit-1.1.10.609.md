# Auditoría Coach 1.1.10.609

## Corrección

- Se corrigió la identificación del jugador local en el autoescáner: la fila resaltada en amarillo y el nombre de la línea se conservan como evidencia del mismo puesto.
- La asignación por slot de pantalla tiene prioridad sobre un mapa de roles antiguo, evitando mostrar el campeón de otra fila como el propio.
- Se mantiene la fila del usuario entre fotogramas cuando el título de línea cambia al nombre del campeón.
- Se eliminó el respaldo que podía presentar el primer campeón aliado como si fuera el del usuario cuando faltaba evidencia de rol.

## Validación

- Se añadieron pruebas para la selección única del resaltado amarillo y para eliminar duplicados de línea al fijar el slot del usuario.
