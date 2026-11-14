# Auditoría Coach 1.1.10.540

- `versionCode`: `1256`.
- `versionName`: `1.1.10.540`.

## Corrección de la validación

- Se revisó la ejecución completa de la integración anterior: la compilación
  del APK, el escáner y las auditorías instaladas pasaron; la única falla fue
  `CalibrationEvidenceImageTest`, porque Robolectric no rasteriza operaciones
  de `Canvas` en esa prueba.
- La selección de regiones de baneos para la evidencia ahora está aislada en
  una función determinista: una observación parcial conserva las coordenadas
  guardadas y una fila completa usa las cinco regiones observadas por índice.
- La prueba dejó de depender de un píxel dibujado por `Canvas` y verifica ambas
  rutas geométricas, además de que la evidencia codificada siga siendo válida.

## Verificaciones

- Se reprodujo el fallo de la suite completa y se identificó el caso exacto.
- Se comprobó el comportamiento de fila parcial y fila completa con pruebas
  unitarias deterministas.
- Se mantuvo la representación visual de avatares y círculos en la aplicación.
