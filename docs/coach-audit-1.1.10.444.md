# Coach 1.1.10.444 — build 1160

## Solicitud y evidencia

El tester muestra una captura 1280×587 con las columnas del visor desplazadas
hacia los bordes. La telemetría visible dice PAUSADO. No se incorporan presets
por fabricante o modelo; dimensiones iguales tampoco garantizan iguales márgenes
del juego, por lo que se ajusta con evidencia de los anillos visibles.

## Cambios

- FloatingOverlayContent: al abrir el visor con escaneo pausado o cambiar sus
  dimensiones, obtener como máximo dos imágenes nuevas para geometría y baneos.
  Se reutilizan configForFrame, las exclusiones y la confirmación temporal de
  baneos. No se asignan campeones seleccionados mediante retratos. Tras la
  actualización puntual, la captura vuelve al mecanismo de suspensión existente.
- DraftRingAlignment: usar el diámetro de la columna medida; si no se puede
  alinear con ese tamaño, probar escalas físicas próximas (0,8 y 1,2) con al menos
  tres anillos coherentes. Actualizar también los diámetros compartidos por visor
  y recortes. No elegir coordenadas a partir de una marca de teléfono.
- DraftRingAlignmentTest: añadir escenas sintéticas con márgenes internos y
  distintos tamaños de HUD en cuatro dimensiones, incluida 1280×587. Añadir la
  clase a las comprobaciones tempranas. Conservar las pruebas de capturas reales,
  máscaras, baneos y confirmación temporal existentes.

## Límites y validación

No se dispone del archivo original del tester como fixture local; no se afirma
haber ejecutado reconocimiento sobre esa captura ni probado el teléfono físico.
Pruebas Android pendientes de ejecución remota. Las escenas sintéticas no
demuestran compatibilidad física universal ni ahorro de batería medido.
Comprobaciones locales correctas: diff, recursos completos en los dos idiomas,
10 pruebas del plan de validación y 15 de reutilización de APK.
La solicitud #188 permanece abierta para los cambios que ya están en validación.
