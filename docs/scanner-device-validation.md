# Validación del escáner

## Evidencia y límites

| Equipo | Estado físico | Evidencia automatizada |
| --- | --- | --- |
| Red Magic | Funciona según reporte del propietario | Captura proporcionada, escalado y perfiles |
| Poco | Funciona según reporte del propietario | Perfil medido conservado y captura escalada |
| S26 | Pendiente: el propietario no lo ha probado | Simulación de resoluciones, no hardware físico |

La captura de referencia permite validar los diez slots laterales y diez baneos
superiores. El arte central no determina la identidad. Las pruebas varían tamaño,
bandas exteriores y arte central sin relajar los umbrales del clasificador.
La matriz geométrica comprueba transformaciones; no certifica reconocimiento a
cualquier dimensión, skin, densidad ni distribución futura del juego.

## Validación desde el teléfono

1. Abrir el visor durante una selección y comprobar los diez recortes y los baneos.
2. Si hace falta, elegir un slot, moverlo o cambiar su tamaño. El visor y el escáner
   comparten el ajuste observado, separado por modelo, orientación y proporción.
3. Pulsar Enviar coordenadas con una cuenta de correo verificada. Esperar la
   confirmación; si hay error, conservar los ajustes y reintentar con conexión.
4. En soporte, abrir el mensaje y pulsar Copiar coordenadas. Incluye versión,
   modelo, dimensiones, densidad, tamaño de fuente y geometría. No incluye una
   captura ni el correo dentro del bloque. Si no hubo frame, dice not_observed.
5. Validar tres fases de baneos, diez selecciones, cambio de orientación,
   persistencia tras reinicio y apertura final de la build. En S26 esto sigue
   pendiente. Un envío de coordenadas no es una medición de latencia ni un PASS.

Para evidencia técnica con tiempos y huellas existe `tools/scanner-device-report.py`.
No convertir las imágenes redimensionadas ni un emulador en registros físicos.
