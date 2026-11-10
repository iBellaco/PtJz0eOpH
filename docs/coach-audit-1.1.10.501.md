# Auditoría del Hub, Auto-Scan y recomendaciones por favoritos de Coach 1.1.10.501

## Versión

- `versionName`: `1.1.10.501`
- `versionCode`: `1217`
- Alcance: visibilidad del estado Auto-Scan y filtro de recomendaciones del Hub por favoritos de la línea.

## Correcciones aplicadas

- El texto `Auto-Scan` activo ahora tiene un pulso visible de opacidad y escala, además de un borde y fondo que acompañan el ciclo. La animación se detiene cuando Auto-Scan está apagado.
- El Hub flotante ahora carga los favoritos guardados del usuario y muestra `Recomendar solo mis favoritos de esta línea` junto a las recomendaciones.
- El filtro se puede activar o desactivar y se habilita únicamente cuando existen al menos cinco favoritos válidos para la línea seleccionada.
- Con el filtro activo, el análisis usa solo ese grupo de favoritos, incluido el modo de primer pick. Si ningún favorito tiene un enfrentamiento favorable, conserva el menos desfavorable dentro del mismo grupo.
- La preferencia se conserva en el dispositivo y se desactiva automáticamente si la línea deja de cumplir el mínimo.

## Verificaciones

- Compilación Kotlin de la aplicación: correcta.
- `DraftRecommendationQualityTest`: correcto, incluidos primer pick, mínimo de cinco favoritos y fallback sin el mínimo.
- `git diff --check`: correcto.
- Pendiente de validación visual en el Hub flotante y con Auto-Scan activo en un dispositivo físico compatible.
