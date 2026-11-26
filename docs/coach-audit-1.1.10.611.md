# Coach 1.1.10.611 · Corrección de la prueba del resaltado del jugador

## Corrección

- Se eliminó la dependencia de utilidades de color del framework Android en la
  clasificación del resaltado amarillo del autoescáner.
- La prueba del resaltado usa valores ARGB directos y puede ejecutarse en la
  suite JVM sin depender de métodos Android no simulados.
- Se incrementó la versión de Coach a `1.1.10.611` (1327).

## Verificación

- Se ejecutará nuevamente la prueba dirigida de identificación del slot del
  jugador dentro de la validación selectiva.
