# Auditoría Técnica y Registro de Modificaciones — Versión 1.1.10.186 (Build 902)

## Resumen Ejecutivo
Esta versión orienta al desarrollador exactamente sobre el enlace "Administrar permisos de la cuenta de servicio" visible en la pestaña de Cuentas de servicio para completar la asignación del rol IAM en Google Cloud.

## Cambios Implementados
- **Pasos Finales de IAM:** Tocar el enlace "Administrar permisos de la cuenta de servicio ↗", editar el usuario `firebase-adminsdk-fbsvc@wild-rift-drafting.iam.gserviceaccount.com`, agregar el rol "Administrador de reglas de Firebase" y guardar.
- **Incremento de Versión:** `versionCode` a 902 y `versionName` a `1.1.10.186` en `app/build.gradle.kts`.

## Archivos Modificados
- `app/build.gradle.kts`
- `docs/coach-audit-1.1.10.186.md`
