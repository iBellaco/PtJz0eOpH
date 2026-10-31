# Coach 1.1.10.227 — código 943

## Limpieza de instrucciones y seguimiento

AGENTS.md sigue siendo la única fuente de instrucciones. INSTRUCCIONES_AGENTES.md contiene únicamente un enlace relativo a ese archivo, sin reglas duplicadas.

El .gitignore conserva las exclusiones secrets.properties, *.keystore, *.jks y *.base64. Los patrones de reparaciones temporales de la raíz (fix_*, patch_*, update_* y temp_*) cubren cualquier extensión, incluidos scripts y JSON. Las listas temporales conocidas mantienen sus exclusiones específicas. Se añaden excepciones explícitas para gradlew, gradlew.bat y gradle/wrapper/gradle-wrapper.jar. Los tres permanecen versionados y gradlew conserva el permiso de ejecución. secrets.defaults.properties sigue siendo la plantilla compartida.

Se revisa git ls-files -ci --exclude-standard y se ejecuta git rm --cached --ignore-unmatch para los temporales y las credenciales indicadas. La revisión inicial no encontró archivos ignorados bajo seguimiento: ya se habían retirado en la limpieza anterior. No se borran archivos locales ni herramientas mantenidas dentro de tools/.

## Archivos modificados

- INSTRUCCIONES_AGENTES.md: enlace único a AGENTS.md.
- .gitignore: patrones de temporales, exclusiones privadas y excepciones del wrapper.
- app/build.gradle.kts: versión 227 y código 943.
- docs/coach-audit-1.1.10.227.md: registro de la intervención.

## Verificación

Se comprueban los patrones de exclusión, la ausencia de archivos ignorados versionados, los componentes y permisos del wrapper y su identidad oficial. La entrega requiere las comprobaciones obligatorias existentes del APK ofuscado, la firma persistente, las pantallas en español y portugués y el APK instalado antes de fusionar. Los resultados finales quedan asociados a la solicitud de cambios y la ejecución de esta versión. Se publican y descargan ambos ZIP para comprobar integridad y que el proyecto coincide con el código fusionado sin archivos privados.

## Resumen para testers

Coach 1.1.10.227: verificar la actualización de la aplicación, la versión mostrada y la navegación en español y portugués. La entrega incluye el APK listo para instalar y el proyecto con instrucciones unificadas y archivos temporales excluidos.

## Commit copiable

chore: unificar instrucciones y proteger archivos locales conservando el wrapper de Coach
