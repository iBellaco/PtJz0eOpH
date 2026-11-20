# Perfil de calibración Vision Draft — Xiaomi 24122RKC7G

Perfil normalizado guardado para el dispositivo Xiaomi `24122RKC7G` y la pantalla física `2400x1080`.
Las capturas alineadas `1920x864` y literal `1280x576` son alias del mismo perfil; no se comparte la calibración con otros modelos por coincidencia de resolución.

## Geometría

- Avatar global: diámetro `0.11`; columna aliada `X=0.11662696`; columna rival `X=0.9165625`.
- Barra superior: `Y=0.045`; diámetro `0.072`; quinto aliado `X=0.16712953`; quinto rival `X=0.9741347`.
- Slots aliados: `X=0.11662696`; `Y=[0.188, 0.324, 0.458, 0.591, 0.725]`; diámetros `[0.105, 0.105, 0.105, 0.105, 0.1]`.
- Slots rivales: `X=0.9165625`; `Y=[0.18861003, 0.32380405, 0.4560159, 0.58822775, 0.71944547]`; diámetro `0.107`.

## Baneos manuales

- Aliados: `X=[0.06875, 0.087500006, 0.13, 0.1725, 0.21499999]`, `Y=[0.05277778, 0.06388889, 0.05138889, 0.043055557, 0.05138889]`, diámetro `0.061111107`.
- Rivales: `X=[0.785, 0.82125, 0.856875, 0.8925, 0.93]`, `Y=[0.05138889, 0.05138889, 0.05138889, 0.047222223, 0.05277778]`, diámetro `0.058333334`.
- Ambos lados quedan marcados como `manual=true` para conservar las cinco posiciones originales sin reordenarlas.

## Evidencia de referencia

La captura aportada validó Sylas aliado 1, Senna rival 1, Gwen rival 2 y Kog'Maw aliado 3. Los baneos confirmados fueron Riven aliado 1, Yunara aliado 5, Mordekaiser rival 2, Mel rival 3, Ekko rival 4 y Caitlyn rival 5. La evidencia literal fue `1280x576`, con SHA-256 `83f2552f0d45911b34e1ee15735c1e4ba523789d27c3b69c46488700a4e15841`.

## Evidencia adicional de respaldo — 2026-11-20

Esta evidencia queda acumulada como referencia para la corrección de calibración
1.1.10.575. La imagen binaria no está disponible para incorporarla como fixture.

### Resultado observado

- La selección de campeones reportada coincide con la captura: Hwei aliado 1,
  Miss Fortune aliado 2, Amumu aliado 4; Lux rival 1, Ashe rival 2 y Nautilus
  rival 3.
- El problema reportado está concentrado en los baneos aliados; no usar estas
  identidades como nueva calibración hasta comparar la siguiente captura.
- Baneos detectados en esta lectura: Riven aliado 1 (0.89), Mordekaiser aliado
  5 (0.97), Vladimir rival 1 (0.96), Aurora rival 2 (0.95), Teemo rival 3
  (0.96), Yunara rival 4 (0.96) y Galio rival 5 (0.90).

### Evidencia de captura

- Dispositivo: Xiaomi 24122RKC7G; pantalla física `2400x1080`; Android 15/API 35.
- Captura alineada: `1920x864`; captura literal: `1280x576`; antigüedad indicada:
  `53 ms`.
- Perfil usado: `true_222`; viewport completo `1920x864`.
- El informe indica seis posiciones con identidad y siete baneos confirmados.
- La imagen original adjunta en la conversación no está disponible como archivo
  local en este entorno; queda pendiente incorporarla como recurso binario de
  respaldo cuando se vuelva a adjuntar como imagen/archivo.

### Estado

Respaldo textual guardado y utilizado junto con la referencia OPPO para probar
que las filas manuales de baneos no se redistribuyen con las columnas de picks.
La corrección de identidad de los baneos aliados sigue pendiente de una imagen
binaria comparable.
