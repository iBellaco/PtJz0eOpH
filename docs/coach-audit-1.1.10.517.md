# Auditoría de Coach 1.1.10.517 (compilación 1233)

## Cambios aplicados

- El formulario de patrocinio bilingüe permite cargar un banner horizontal independiente para Español y Português, valida que ambos existan y conserva los medios por idioma hasta la publicación.
- El presupuesto del patrocinio conserva el cargo real en esencias y muestra el equivalente reservado en USD; la publicación aprobada ya no pierde el importe al pasar del estado pendiente al anuncio público.
- El inicio selecciona el banner según el idioma del usuario y mantiene compatibilidad con anuncios antiguos de un solo medio.
- El botón de diagnóstico queda primero en el Hub, mantiene el límite diario de cinco envíos y los reportes de tipo diagnóstico/admin-only no se muestran en la bandeja del usuario.
- Auto-Scan deshabilitado comunica su estado con animación roja y el texto activo se ajusta; el botón de inicio conserva una animación de reposo ligera.
- Premium muestra el estado de recomendaciones de draft basado en favoritos por línea; la tier list oculta campeones sin estadísticas competitivas actuales.
- El acceso al visor incorpora búsqueda por nombre de usuario, nombre visible, username o correo.
- Se actualizaron las tarjetas de información inicial en español y portugués.

## Verificación

- `npm test` en `server/economy` (52 pruebas correctas).
- `python3 .github/scripts/validate-localized-resources.py`.
- `bash tools/audit-portuguese.sh` (cero hallazgos de español en superficies localizadas).
- `git diff --check`.
- La compilación Android se intentó con `./gradlew :app:compileDebugKotlin --no-daemon`, pero este entorno no tiene Android SDK configurado (`local.properties`/`ANDROID_HOME`); el flujo de GitHub Actions debe ejecutar la compilación completa.
