# Coach 1.1.10.578 (1294) · Conserva el centro exacto de los baneos medidos

## Problema resuelto

- La versión anterior corrigió los estados vacíos de las filas adaptativas, pero
  una prueba todavía mostraba que los cinco centros medidos de Samsung se movían
  `0.002` al confirmar la identidad.
- La asignación inicial usaba la geometría medida, pero la confirmación temporal
  volvía a copiar la región detectada y sobrescribía ese centro.

## Corrección

- Las filas manuales o medidas conservan el centro configurado durante las fases
  pendiente y confirmada.
- La identidad y la similitud siguen procediendo de la observación real.
- Las filas adaptativas mantienen el comportamiento dinámico validado en la
  ejecución anterior.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/service/screen/BannedPortraitScanner.kt`

## Verificación

- La ejecución anterior completó 304 pruebas y dejó un único fallo reproducible.
- El ajuste actúa exactamente en la asignación que produjo la diferencia de
  coordenadas y conserva sin cambios el reconocimiento de identidad.
- La comprobación local de Gradle sigue limitada por la descarga bloqueada de
  Gradle 9.3.1; la suite dirigida se ejecuta en la validación remota.
