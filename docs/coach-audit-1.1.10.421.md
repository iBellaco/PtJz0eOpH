# Coach 1.1.10.421 — build 1137

## Causa y corrección

La ejecución 40964217376 falló al compilar Kotlin tanto para debug como para
release. Los registros de los trabajos 123764780981 y 123764781001 coinciden:
`AdminDashboardDialog.kt`, líneas 132–135, llama a `CoachTextButton` aunque el
archivo lo importa como `TextButton`. El alias oculta el nombre original y
provoca dos referencias sin resolver y cuatro errores Compose derivados.

Se usan las dos llamadas `TextButton` con el alias ya existente. No se
modifican acciones, permisos, tiempos, etiquetas de prueba ni tamaños táctiles.
La prueba rápida de regresión comprueba el alias, el reintento con su bloqueo
durante la consulta y el cierre disponible con tamaño mínimo de 48 dp.
Se ejecuta antes de las pruebas Android; no reemplaza al compilador.

Los trabajos de catálogos, traducciones y operaciones económicas de la ejecución
anterior terminaron correctamente. No se modifica su lógica ni se desactivan
comprobaciones para conseguir un resultado verde.

## Archivos

- `app/src/main/java/com/example/ui/components/AdminDashboardDialog.kt`.
- `app/build.gradle.kts`: versión 1.1.10.421, código 1137.
- `.github/scripts/test_session_dialog.py`.
- `.github/workflows/fix-error.yml`.
- `AGENTS.md`: nueva preferencia explícita de respuesta, sin commits ni reportes
  para testers y sin referencias internas en el texto de actualización.
- Este documento.

## Verificación

Tres comprobaciones de regresión textual del diálogo, validación de recursos
localizados, pruebas del plan de validación y `git diff --check`. La compilación
Android completa queda pendiente de la nueva ejecución: el entorno local no
tiene SDK Android y anteriormente no pudo descargar Gradle. No se afirma que
exista un APK compilado o instalado de esta versión.

Entrega únicamente en `fix-error`. Sin promoción adicional ni cambios en `main`.
