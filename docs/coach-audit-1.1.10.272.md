# Auditoría de builds contextualizadas — 1.1.10.272

## Alcance

- El detalle del campeón muestra una build 1 contra 1 calculada únicamente con el rival de la línea.
- Cuando hay cinco rivales identificados, muestra una segunda build calculada con la composición completa.
- Cada plan expone el alcance del cálculo y los campeones considerados para que no se confundan entre sí.
- Objetos, runas y hechizos se muestran con imágenes del catálogo local.

## Evidencia

- `DraftRecommendationQualityTest` verifica que los dos planes reciban conjuntos de rivales distintos y con etiquetas de contexto distintas.
- La auditoría renderizada conserva el detalle dentro de la ficha del campeón.
- La versión Android se incrementó a `versionCode 988`.
- El plan contra composición completa conserva los cinco rivales en el orden recibido.
