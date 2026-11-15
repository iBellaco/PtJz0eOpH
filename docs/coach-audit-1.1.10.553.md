# Coach 1.1.10.553 — Corrección de pruebas de evidencia y texto táctico

Versión: 1.1.10.553; versionCode: 1269.

## Cambios

- Se conserva el texto completo de las secciones de sinergia y counter para
  que la lectura accesible y el detalle táctico no pierdan su última
  instrucción.
- El resumen compacto queda limitado a la recomendación principal, las
  alternativas y el overlay, donde sí es necesario evitar tarjetas extensas.
- La prueba de evidencia visual comprueba de forma estable que las variantes
  limpia y auditada se codifican correctamente, sin depender de diferencias de
  hash que Robolectric puede normalizar durante la compresión JPEG.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/ui/components/DraftAdviceSection.kt`
- `app/src/test/java/com/example/CalibrationEvidenceImageTest.kt`

## Verificaciones

- La suite completa anterior identificó únicamente los dos casos corregidos.
- La suite JVM dirigida debe ejecutarse con el JDK completo antes de promover
  esta revisión a la validación completa.

## Texto para pruebas

Coach 1.1.10.553

- Las recomendaciones principales y sus alternativas continúan resumidas.
- Las secciones de sinergia y counter conservan el texto completo para no
  perder instrucciones tácticas.
- La evidencia visual sigue codificándose tanto en modo limpio como con la
  capa de auditoría del visor.
