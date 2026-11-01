# Coach 1.1.10.442 — build 1158

Primer bloque de implementación de los dos informes del 01/11/2026. La petición
completa incluye 54 recomendaciones; este bloque NO las completa todas.

## Implementación

- `WebMediaPolicy.kt`, `NoticeMediaViewer.kt`: autoridad HTTPS exacta, rechazo de
  credenciales/puertos inesperados, navegación dentro del proveedor seleccionado,
  sin acceso local desde visores remotos ni contenido mixto. Se conservan los
  reproductores nativos de medios locales. Hace falta comprobar reproducción real.
- `file_paths.xml`: quitar acceso a toda la caché; compartir únicamente recortes.
- `DiagnosticPrivacy.kt`, `Logger.kt`, `CrashLogger.kt`: redacción limitada de
  correos, tokens y consultas URL; límite de entradas y rotación del archivo de
  errores. No garantiza ausencia de datos sensibles en otros registros directos.
- `UserPreferences.kt`, `TenthPickDiagnosticManager.kt`: diagnóstico desactivado
  por defecto y nueva preferencia de consentimiento explícito. No se borra material
  existente. Retención temporal y controles de vista previa siguen pendientes.
- `BackupRestoreManager.kt`: límite de 8 MiB, versión/formato/cantidades/IDs,
  arrays de selecciones, transacción Room y exclusión mutua entre restauraciones;
  deduplicación dentro de una importación combinada y propagación de cancelación.
  La transacción protege el historial. Perfiles en preferencias y Room NO constituyen
  todavía una transacción conjunta resistente a la muerte del proceso.
- `GlobalMetaSyncService.kt`: consultas cada cinco minutos cuando solo consume la
  burbuja, exclusión de sincronizaciones simultáneas y comprobación de versión sin
  acumular corrutinas. No se afirma ahorro medido en dispositivos.
- `AppUpdateManager.kt`, `MainActivity.kt`, `AppUpdateDialog.kt`: actualización
  ordinaria posponible durante el proceso actual; solo obligatoria si el mínimo
  compatible confirmado supera la versión instalada. Campo opcional
  `minimumSupportedVersionCode` en la configuración existente; sin valor, cero.
  No se cambió ni desplegó configuración remota.
- `RememberAttachment.kt`, `UserInboxDialog.kt`: decodificación fuera del hilo
  principal, dos trabajos simultáneos y muestreo de dimensiones. Falta migrar los
  otros visores y comprobar memoria en dispositivos.
- Recursos nuevos completos en español y ambas variantes portuguesas.

## Verificación

- Recursos localizados, pruebas de recursos y diálogo de sesión: correctos localmente.
- Plan de validación (10 pruebas) y reutilización de APK (15 pruebas): correctos.
- Validación del contenido del catálogo: correcta.
- Añadidas pruebas de dominios fraudulentos, redirecciones, redacción, actualización
  posponible/obligatoria, respaldo incompatible y rollback ante fallo de inserción.
  Incluidas en la selección de fix-error. Ejecución Kotlin/Android pendiente.
- `bash gradlew --version`: falló por conexión rechazada al descargar Gradle.
  No hay evidencia local de compilación release, APK instalado ni ahorro físico.
- Se conserva el arreglo anterior de limpieza independiente.

Seguimiento completo: `docs/recommendations-tracker.md`.
