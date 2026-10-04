# Auditoría Técnica y Registro de Modificaciones — Versión 1.1.10.176 (Build 892)

## Resumen Ejecutivo
Esta versión corrige de raíz los fallos detectados en la suite de pruebas unitarias y de renderizado de interfaces durante la ejecución de integración continua en la nube. Se desacopló la inicialización del servicio de autenticación en componentes visuales y utilidades de roles para evitar excepciones de estado en entornos de prueba aislados, se eliminó la dependencia de simulación dinámica en el panel de perfil autenticado y se garantizó la compatibilidad total del ciclo de validación.

## Cambios Implementados

### 1. Desacoplamiento Seguro de la Inicialización de Autenticación
- **`RolePanelAccess.kt` y `SupportTicketAccess.kt`:** Se sustituyeron las llamadas directas al proveedor de autenticación por el acceso seguro `AuthManager.getAuth()`, evitando excepciones cuando los servicios no han sido instanciados previamente.
- **`SubscriptionManager.kt`:** Se ajustó la resolución del correo electrónico del usuario activo mediante `AuthManager.getAuth()?.currentUser?.email`.

### 2. Eliminación de Simulación Dinámica en Pruebas de Renderizado
- **`AuthScreen.kt` (`AuthenticatedProfilePanel`):** Se flexibilizó el parámetro de usuario para admitir valores nulos de forma segura, derivando identificadores, correo y nombre visible a partir del perfil activo en memoria y protegiendo las consultas en segundo plano.
- **`RuntimeVisibilityTest.kt`:** Se eliminó la inicialización dinámica de simulaciones sobre clases abstractas de autenticación que generaba errores de complemento en Java 21, permitiendo la renderización determinista y aislada del componente de perfil.

### 3. Incremento de Versión y Trazabilidad
- `app/build.gradle.kts`: Incremento de `versionCode` a 892 y `versionName` a `1.1.10.176`.

## Archivos Modificados
- `app/src/main/java/com/example/model/RolePanelAccess.kt`
- `app/src/main/java/com/example/data/SupportTicketAccess.kt`
- `app/src/main/java/com/example/util/SubscriptionManager.kt`
- `app/src/main/java/com/example/ui/auth/AuthScreen.kt`
- `app/src/test/java/com/example/RuntimeVisibilityTest.kt`
- `app/build.gradle.kts`
- `docs/coach-audit-1.1.10.176.md`
