# Auditoría Técnica y Registro de Modificaciones — Versión 1.1.10.178 (Build 894)

## Resumen Ejecutivo
Esta versión consolida la sincronización de credenciales de infraestructura en la nube y el esquema de configuración de servicios para la aplicación. Se verificó la consistencia entre los identificadores de paquete y configuración en la nube, y se documentan los pasos requeridos en el gestor de identidades para la publicación automatizada de reglas.

## Cambios y Diagnóstico de Infraestructura

### 1. Configuración de Credenciales de Despliegue en la Nube
- **Diagnóstico del error 403 en publicación de reglas:** La cuenta de servicio provista (`firebase-adminsdk-fbsvc@wild-rift-drafting.iam.gserviceaccount.com`) requiere tener asignado el rol IAM de **Administrador de reglas de Firebase** (`roles/firebaserules.admin`) o **Administrador de Firebase** en la consola de Google Cloud (IAM) para autorizar la publicación remota de políticas de seguridad.
- **Configuración del Secreto de Automatización:** En el repositorio de GitHub (Settings -> Secrets and variables -> Actions), la clave JSON de la cuenta de servicio debe almacenarse bajo el nombre de secreto `COACH_FIREBASE_SERVICE_ACCOUNT` o `FIREBASE_SERVICE_ACCOUNT` para que el flujo de integración continua complete el despliegue automático en la rama principal.

### 2. Mapeo de Identificadores de Paquete
- Se mantiene la compatibilidad dual en la configuración de servicios para reconocer tanto el identificador de distribución `com.Coach` como el paquete registrado en la consola `com.aistudio.wildriftdrafting.wrdftx`.

### 3. Incremento de Versión
- `app/build.gradle.kts`: Incremento a `versionCode` 894 y `versionName` 1.1.10.178.

## Archivos Modificados
- `app/build.gradle.kts`
- `docs/coach-audit-1.1.10.178.md`
