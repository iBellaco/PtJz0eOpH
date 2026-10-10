# Auditoría Coach 1.1.10.252

- Build: 968.
- Causa: la fila de sustitución de runas tenía un único manejador de clic. El icono de la runa original propagaba el toque y abría la alternativa.
- Corrección: cada icono tiene su propio destino, efecto de catálogo y consejo contextual. La fila no recibe clics. Ambos botones tienen 48 dp y etiquetas independientes para pruebas.
- Alcance: todas las sustituciones de runas de las builds, sin depender del campeón. Los objetos y hechizos tienen manejadores individuales y no comparten este problema de propagación.
- Archivos: `ChampionDetailSheet.kt`, `BuildCoachingRenderedTest.kt`, `app/build.gradle.kts` y este informe.
- Regresión: la prueba renderizada ahora pulsa ambos lados de cada pareja visible y verifica título, efecto y ausencia del consejo de la alternativa en la ficha original, en español y portugués.
- Validaciones: pendientes las comprobaciones de compilación, pruebas, ofuscación, firma e instalación de Actions antes de publicar.
