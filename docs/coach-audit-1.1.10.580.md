# Coach 1.1.10.580 (1296) · Posición de selección en capturas originales

La prueba DraftNativePanelTest compara el centro de cada recorte de producción con el centro de la posición visible en el archivo. El manifiesto app/src/test/resources/scanner/native-panels.json registra la huella SHA-256, el tamaño del archivo y los centros de referencia en píxeles. Un valor nulo indica que el visor impide medir el puesto. La tolerancia es de 6 píxeles de la imagen original. El resultado corresponde al primer ajuste con la geometría automática inicial.

La validación de fix-error.yml incluye esta prueba y DeviceDraftProfileTest dentro de la misma ejecución. El CSV de posiciones se conserva junto con los resultados de las regresiones. Las pruebas de perfiles comprueban las coordenadas guardadas para cada modelo y resolución. El conteo de capturas visuales se obtiene exclusivamente de los archivos originales. La versión de esta auditoría figura en app/build.gradle.kts.

## Adjuntos con informe disponible

| Modelo | Pantalla física | Adjunto | Escena | SHA-256 indicado en el informe |
| --- | --- | --- | --- | --- |
| Xiaomi 24122RKC7G | 2400 × 1080 | 1280 × 576 | Sylas y Senna | 83f2552f0d45911b34e1ee15735c1e4ba523789d27c3b69c46488700a4e15841 |
| Xiaomi 24122RKC7G | 2400 × 1080 | 1280 × 576 | Hwei y Lux | aa2ca4d83f3a364941045b3587b0cfa7833a1ef0020f6ce5481e0d2de66b02f3 |
| OPPO CPH2791 | 2354 × 1080 | 1280 × 587 | Kindred y Lux | 1492d917cadaa6bfa4995da0a7684845b3f9e639519ea3a4300a8c02f54bf7f2 |
| Samsung SM-G998B | 2400 × 1080 | 1280 × 576 | Zoe y Aurora | 25e5a62c46a699653f3ca5cfd8ff8dbfc4930333c411a4c100922985aab94d49 |
| Samsung SM-S938B | 2340 × 1080 | 1280 × 590 | Lucian y Rammus | a55f2e7bc8b493d87dc1eb83dde27a9ab702ed80740fd2bf3b7416fbc3e4ebec |

Los cinco archivos de esta tabla están pendientes de recuperación. Para incorporar un original, conservar sus bytes, verificar el tamaño y la huella, y marcar como referencia solo los círculos del juego distinguibles detrás del visor. El archivo S26 de 1280 × 591 presente en las pruebas corresponde a otra escena.

## Verificación

La medición inicial sobre ocho archivos originales cubrió 75 puestos visibles. Hubo 12 desviaciones superiores a la tolerancia, nueve en poco-header.jpg, dos en red-magic-header.jpg y una en split-header.jpg. Cinco puestos quedaron ocultos en la imagen S26.

La ejecución local con Android y Robolectric reprodujo las 12 desviaciones de selección y pasó las 11 pruebas de DeviceDraftProfileTest. También pasó la prueba de la fila de baneos sobre el archivo original poco-7457.png de 1280 × 576. Esa imagen solo documenta la cabecera.
