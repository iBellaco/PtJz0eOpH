# Perfil de calibración Vision Draft — Samsung SM-S938B

Referencia física aportada para Coach: Samsung `SM-S938B`, Android 17/API 37,
pantalla `2340x1080`. Las capturas alineadas `1920x886` y literal `1280x590`
son alias del mismo dispositivo; no se comparte este perfil con otro modelo
por coincidencia de resolución.

## Geometría de selección

- Diámetro global: `0.11`.
- Columna aliada: `X=0.1140625`; columna rival: `X=0.9164063`.
- Slots aliados: `X=[0.1140625, 0.1140625, 0.06953125, 0.06953125, 0.06953125]`;
  `Y=[0.1928934, 0.32787907, 0.42808804, 0.5578877, 0.6886633]`;
  `diametro=[0.11, 0.11, 0.11, 0.11, 0.105]`.
- Slots rivales: `X=[0.9164063, 0.9164063, 0.9164063, 0.9164063, 0.9164063]`;
  `Y=[0.18819442, 0.32350618, 0.45583317, 0.5881601, 0.71949214]`;
  `diametro=[0.11, 0.11, 0.11, 0.11, 0.11]`.

## Baneos medidos

El informe original indica `Manual=false`; se conserva ese valor. La aplicación
ahora diferencia una medición física de un ajuste manual y usa esta fila medida
sin redistribuirla con la cuadrícula adaptativa.

- Aliados: `X=[0.06923077, 0.10512821, 0.14166667, 0.17884615, 0.21730769]`;
  `Y=[0.05138889, 0.048611112, 0.05, 0.048611112, 0.048611112]`;
  `diametro=[0.06388889, 0.06388889, 0.06388889, 0.06388889, 0.06388889]`.
- Rivales: `X=[0.7826923, 0.8211538, 0.85641026, 0.8923077, 0.9301282]`;
  `Y=[0.05138889, 0.05138889, 0.048611112, 0.047222223, 0.05277778]`;
  `diametro=[0.06388889, 0.06388889, 0.06388889, 0.06388889, 0.06388889]`.

## Barra superior y evidencia

- Barra superior: `Y=0.045`, diámetro `0.072`.
- Quinto círculo superior aliado: `X=0.17138515`.
- Quinto círculo superior rival: `X=0.9734761`.
- Selecciones reportadas: Lucian, Seraphine, Annie, Pantheon y Mordekaiser
  aliados; Rammus, Aurora, Twitch, Lulu y Cho'Gath rivales.
- Baneos reportados: Morgana, Volibear, Lux y Darius aliados; Sylas, Senna,
  Darius, Hwei y Nocturne rivales.
- Captura literal: `1280x590`, antigüedad `26 ms`, perfil `true_217`, viewport
  alineado `1920x886`.
- SHA-256 de la evidencia aportada:
  `a55f2e7bc8b493d87dc1eb83dde27a9ab702ed80740fd2bf3b7416fbc3e4ebec`.

La imagen binaria adjunta no está disponible como archivo local en este entorno;
queda registrada la evidencia textual y sus coordenadas, sin crear una imagen
sustituta ni declarar un respaldo binario inexistente.

## Corrección aplicada

Cuando se conoce el lado del décimo pick, el detector excluye su quinto círculo
superior de la búsqueda de baneos y también de la extrapolación del quinto puesto.
Además, cualquier identidad ya confirmada como selección se descarta de la fila
de baneos antes de publicar el resultado. Esto evita duplicar Twitch o Cho'Gath
en la cabecera de baneos.
