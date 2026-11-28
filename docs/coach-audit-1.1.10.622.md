# Auditoría de Coach 1.1.10.622

- `versionCode`: 1338
- `versionName`: 1.1.10.622

## Corrección incluida

- Las pruebas visuales del draft se alinearon con la regla de selección actual: cuando el usuario ya eligió su campeón, las recomendaciones genéricas no se muestran. La sección solo puede mostrarse si recomienda exactamente al campeón elegido.
- Se conservó la cobertura de los casos sin campeón propio (solo rival o marcador vacío), donde las recomendaciones siguen disponibles.
- Se añadió una expectativa explícita para el draft con ambos equipos seleccionados, evitando que una modificación futura vuelva a mostrar alternativas no solicitadas tras elegir campeón.

## Verificación

- `:app:testDebugUnitTest --tests com.example.RuntimeVisibilityTest`: correcto.
