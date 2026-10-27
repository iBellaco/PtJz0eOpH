# Coach 1.1.10.393 — build 1109

## Fallo exacto de validación

Ejecución 40375001219, revisión 8de3741a, trabajo core-and-rendered
121757008316: OfflinePortugueseAuditTest falló con
`Missing Portuguese resources in values-pt-rBR`.
457 pruebas ejecutadas: 1 fallo y 1 omitida. Faltaban exactamente
`overlay_app_settings` y `overlay_restricted_help` en el catálogo brasileño.
La ayuda nueva se había añadido solamente a values y values-pt.
La prueba existía en la validación completa, pero no estaba incluida en fix-error.

Los 16 grupos nativos del escáner, su auditoría agregada y la compilación APK
aprobaron. La entrega y el informe final quedaron rojos como consecuencia del
fallo de traducciones: no se modifica el escáner para corregir este problema.

## Corrección y prevención

- Se completan las dos traducciones en values-pt-rBR sin relajar las pruebas.
- El error de cobertura Kotlin ahora enumera las claves ausentes.
- La prueba de ayuda de permisos cubre es, pt y pt-BR, incluido el botón.
- Fix-error ejecuta OfflinePortugueseAuditTest además de sus casos anteriores.
- Un validador XML sin SDK comprueba ambos catálogos portugueses antes de iniciar
  los trabajos costosos de fix-error y al comienzo de catalog-validation del
  flujo completo. Detecta claves faltantes/duplicadas y XML inválido. No sustituye
  la auditoría Kotlin de contenido ni el OCR nativo. Se conservan resultados JUnit
  de las pruebas aisladas como artefacto, incluso cuando fallan.

## Evidencia local

El validador nuevo reprodujo el defecto antes de completar las traducciones y
enumeró ambas claves. Sus seis pruebas de regresión pasan, incluida una réplica
del catálogo brasileño incompleto. La compilación debug y 69 pruebas Kotlin/JVM
aprobaron sin fallos ni omisiones: 9 de auditoría portuguesa (incluida la prueba
exacta que falló), 5 de permisos/idiomas, 5 de perfiles, 29 de atlas, 9 de anillos,
10 de lector de títulos y 2 de cobertura de los 142 nombres.
También pasan 13 pruebas de informes/secuencias, 4 de informe de fallos, 2 de
contenido, 4 de versión y la validación del catálogo. YAML y diff comprobados.
La nueva ejecución remota queda pendiente; no se presenta como aprobada.

Archivos: values-pt-rBR/strings.xml, OfflinePortugueseAuditTest.kt,
SystemPermissionHelperTest.kt, validate-localized-resources.py,
test_localized_resources.py, fix-error.yml, build-apk.yml, app/build.gradle.kts
y este informe.

La corrección se envía primero a fix-error. Main y coach-validacion no se cambian
en esta intervención. No se reactiva vigilancia periódica ni se confunden las
pruebas con capturas/emulador con validaciones en teléfonos físicos.
