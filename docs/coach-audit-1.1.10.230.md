# Coach 1.1.10.230 — código 946

## Cambios

- `.github/workflows/firestore-rules.yml`: se detecta la denegación HTTP 403 para crear índices con `grep`, disponible en el runner de GitHub. El mensaje queda como advertencia y continúa la publicación de reglas.
- `.github/workflows/build-apk.yml`: los errores de lint se muestran con la misma herramienta disponible en el runner.
- `app/build.gradle.kts`, `README.md`: versión 1.1.10.230, código 946.

## Verificación

La versión 1.1.10.229 pasó seguridad, economía, lint, pruebas Android, ofuscación, firma e instalación en español y portugués. Sus ZIP públicos se descargaron y pasaron SHA-256 e integridad ZIP. El despliegue de reglas se detuvo porque el runner no tenía `rg`; esta versión sustituye esa llamada y se comprueba en Actions antes de entregar.

## Resumen para testers

Coach 1.1.10.230: comprobar que los permisos de cuentas suspendidas funcionan, que las solicitudes antiguas avanzan antes que las nuevas y que el APK instalado muestra la versión actual en español y portugués.
