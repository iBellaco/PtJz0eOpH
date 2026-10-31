# Coach 1.1.10.138 — auditoría de portugués

## Alcance

- Se recorrieron las cadenas del catálogo portugués y sus alias, los 300 consejos de build y el texto localizado de campeones, habilidades, objetos, runas y hechizos.
- Se detectaron residuos españoles y traducciones mezcladas en recomendaciones tácticas, etiquetas de campeones y descripciones de contenido. Se centralizó una normalización portuguesa aplicada después de las traducciones exactas y de plantilla para cubrir también textos reutilizados desde datos.
- Se amplió la prueba offline para inspeccionar consejos de build, catálogos y frases mixtas; las pruebas de regresión de contenido recorren 142 campeones, 710 análisis por rol y 2.770 campos localizados.

## Verificación

- `OfflinePortugueseAuditTest`: 6 pruebas pasan en el entorno JVM disponible.
- Auditoría de contenido: 142 campeones, 710 análisis por rol, 2.770 campos localizados; evoluciones de objetos resueltas.
- El checkout local no contiene Gradle Wrapper ni Android SDK; el workflow de GitHub ejecuta las pruebas Android y ensambla el APK para el PR.

## Revisión manual pendiente

La normalización corrige residuos y formas recurrentes, pero no sustituye una revisión lingüística nativa de cada frase. Se recomienda revisar visualmente las pantallas principales en portugués brasileño después del build de CI.
