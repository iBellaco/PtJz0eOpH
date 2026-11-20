# Perfil de calibración Vision Draft — OPPO CPH2791

Perfil normalizado guardado para el dispositivo OPPO `CPH2791` y la pantalla
física `2354x1080`. Las capturas alineadas `1920x881` y literal `1280x587`
son alias de este perfil; no se comparte con otros modelos por coincidencia de
resolución.

## Geometría

- Avatar global: diámetro `0.11`; columna aliada `X=0.12303629`; columna rival
  `X=0.90703124`.
- Barra superior: `Y=0.045`; diámetro `0.072`; quinto aliado
  `X=0.17041795`; quinto rival `X=0.9736258`.
- Slots aliados: `X=0.12303629`; `Y=[0.188, 0.324, 0.458, 0.591, 0.725]`;
  diámetros `[0.11, 0.11, 0.11, 0.11, 0.105]`.
- Slots rivales: `X=0.90703124`; `Y=[0.18950696, 0.3253833, 0.45826235,
  0.5911414, 0.72302145]`; diámetro `0.11`.

## Baneos manuales

- Aliados: `X=[0.078402996, 0.11492116, 0.15143925, 0.18795736, 0.22447549]`,
  `Y=[0.052, 0.052, 0.052, 0.052, 0.052]`, diámetro `0.06`.
- Rivales: `X=[0.77501595, 0.8100701, 0.8470363, 0.8833652, 0.9203314]`,
  `Y=[0.05, 0.048611112, 0.048611112, 0.05, 0.05277778]`, diámetro `0.06`.
- Ambos lados quedan marcados como `manual=true`. La fila aliada se conserva
  separada de la geometría de los slots porque la evidencia reporta un error de
  posición en esos baneos.

## Evidencia de detección

Las selecciones reportadas fueron Kindred, Lux, Kai'Sa, Garen y Brand en el
lado rival. Se registró Cho'Gath en el baneo aliado 4 y Urgot, Vex, Pyke,
Dr. Mundo y Zed en los cinco baneos rivales. Esta lectura se conserva como
referencia de prueba, no como confirmación de que la fila aliada ya esté
correcta.

La captura literal indicada fue `1280x587`, la captura alineada `1920x881` y el
viewport completo. La imagen adjunta en la conversación no está disponible como
archivo binario dentro del entorno actual; queda pendiente incorporarla como
recurso de respaldo cuando se vuelva a adjuntar como archivo.

## Estado de integración

El perfil y las pruebas geométricas quedan preparados en `fix-error`. La
validación física de la imagen y la corrección final de identidad/posición de
los baneos aliados requieren el archivo original o una nueva captura comparable.
