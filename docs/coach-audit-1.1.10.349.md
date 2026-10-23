# Coach 1.1.10.349 — build 1065

## Alcance

Amplía la auditoría reproducible del escáner usando exactamente la captura del
propietario de 1280 × 579 (SHA-256
`42661aefae3df52b4f72047c039120f72f4ea351139d7bdb5107351fe8d3342a`).
No cambia las coordenadas productivas confirmadas de Red Magic ni Poco.

La matriz ampliada detectó desplazamientos de centros en tres variantes:
1800 × 1800 ajustado con bandas, y paneles anclados de 640 × 360 y 1024 × 768.
El buscador de anillos ahora pondera la distancia radial y refina a un píxel
la búsqueda gruesa de tres píxeles. Conserva el mínimo de evidencia circular,
los umbrales del clasificador y la tolerancia geométrica de la prueba.
Dos bordes parciales no justifican estirar las cinco filas: la recuperación
con solo dos observaciones exige 70 % de cobertura circular o un desplazamiento
inequívoco de más de 1.25 radios respecto al perfil previo. Las filas conservan
el ajuste robusto de la cuadrícula en lugar de volver a un borde individual ruidoso.
Una regresión enfocada de los tres formatos y de Red Magic/Poco se ejecuta antes
de la matriz completa.

## Pruebas

- 54 perfiles de pantalla y distribución: 6 referencias/escenarios S26, 20
  resoluciones genéricas de teléfono, 4 de tableta, 4 de plegable, 10 con paneles
  laterales anclados y 10 asociados a modelos Android con fuentes documentadas.
- Cada perfil ejecuta original, centro cambiado y retratos/baneos reordenados:
  162 casos y 3240 comprobaciones de identidad si todos se completan.
  La tercera variante reemplaza toda la escena y conserva únicamente los veinte
  recuadros de retratos, por lo que no depende del fondo, nombres ni arte central.
- Cada retrato debe superar el clasificador productivo y confirmarse mediante
  dos frames; cada baneo pasa por dos llamadas al escáner productivo. Se verifica
  que la configuración publicada al visor coincide con la del recorte.
- El error máximo permitido es 6 píxeles sobre la referencia de 1280 × 579.
  El informe conserva el error medido, puntuación, coordenadas y confirmaciones.
- El generador rechaza posiciones duplicadas, variantes o grupos ausentes,
  identidades erróneas, confirmaciones ausentes y una captura de referencia distinta.

## Entregables de la ejecución

El artefacto `scanner-multidevice-audit` conserva `audit-report.md`,
`audit-measurements.csv`, `audit-summary.json`, resultados por grupo y capturas
con los círculos de slots y baneos. Cada fila distingue tamaño de pantalla,
captura limitada a 1920 × 1080, variante y tipo de distribución.
La suite existente comprueba además 5133 transformaciones geométricas;
esas transformaciones no se cuentan como reconocimientos de imágenes.

## Archivos

- `DraftMultiDeviceAuditTest.kt`: perfiles, variaciones y confirmación productiva.
- `DraftReferenceFrameTest.kt`: evidencia de las transformaciones geométricas.
- `DraftRingAlignment.kt`: precisión del ajuste de los bordes circulares.
- `tools/scanner-audit-summary.py`: comprobación estricta e informes Markdown/CSV/JSON.
- `tools/test_scanner_audit_summary.py`: pruebas positivas y negativas del informe.
- Workflow de validación y versión de la aplicación.

## Límites

No hay teléfonos conectados. Red Magic y Poco funcionan según el propietario;
no se presenta una prueba física nueva. S26 conserva escenarios explícitamente
simulados, sin inventar sus especificaciones. Las dimensiones documentadas de
otros modelos no certifican su hardware, sistema operativo o rendimiento.
Las permutaciones prueban los retratos de esta captura, no todas las skins.
Una matriz finita no prueba todos los teléfonos Android ni futuras distribuciones.
La compilación, las pruebas Android y el APK instalado deben quedar validados
en Actions antes de fusionar y publicar la entrega.
