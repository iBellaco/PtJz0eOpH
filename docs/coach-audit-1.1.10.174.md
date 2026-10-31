# Auditoría Técnica y Registro de Modificaciones — Versión 1.1.10.174 (Build 890)

## Resumen Ejecutivo
En esta versión se resolvieron las incidencias de sincronización de permisos y métricas reportadas en los módulos de transmisión en vivo y gestión de esencias, y se implementó la visualización en tiempo real del conteo de clics en las publicaciones de canales. Adicionalmente, se creó el protocolo maestro para trabajo conjunto entre agentes.

## Cambios Implementados

### 1. Contador de Clics en Publicaciones de Canales
- **Panel de verificación de transmisiones (`StreamerReviewPanel`):** Se integró un listener en tiempo real para las métricas de clics asociados a cada publicación aprobada.
- **Tarjeta de canal aprobado (`ApprovedStreamerReviewCard`):** Ahora muestra explícitamente el contador de clics obtenidos por la publicación.
- **Panel del creador / streamer (`StreamerPanelDialog`):** Al tener una publicación activa en vivo, el creador puede ver de inmediato cuántos clics ha recibido su transmisión en curso.
- **Historial de publicaciones (`StreamerPublicationHistory`):** En publicaciones aprobadas o finalizadas sin conteo previo registrado, se muestra el valor predeterminado (0 clics) en lugar de un mensaje de dato no disponible.

### 2. Estabilización de Permisos y Acceso en la Nube
- **Reglas de acceso (`firestore.rules`):**
  - Se ampliaron y normalizaron las funciones de verificación de roles (`isAdmin`, `isModerator`, `isStreamer`, `canRedeemEssence`) para soportar de manera insensible a mayúsculas/minúsculas los roles y marcos especiales.
  - Se vinculó explícitamente la cuenta institucional principal para evitar denegaciones de permisos en la administración de canales y solicitudes de pago.
  - Se aseguraron comparaciones en minúsculas en correos electrónicos para solicitudes de soporte y canjes de esencias.
- **Normalización de roles en cliente (`SubscriptionManager`, `SupportTicketAccess`, `RolePanelAccess`):**
  - Homogeneización en la detección de privilegios institucionales en la capa cliente.

### 3. Protocolo Maestro de Relevo entre Agentes
- Creación de `INSTRUCCIONES_AGENTES.md` con las pautas de lectura previa, reglas de escritura, incremento de versiones, bilingüismo estricto y formato de entrega.
- Actualización de `AGENTS.md` con enlace al protocolo maestro.

## Archivos Modificados
- `firestore.rules`
- `app/src/main/java/com/example/util/SubscriptionManager.kt`
- `app/src/main/java/com/example/data/SupportTicketAccess.kt`
- `app/src/main/java/com/example/model/RolePanelAccess.kt`
- `app/src/main/java/com/example/ui/components/StreamerPanels.kt`
- `app/src/main/java/com/example/ui/components/StreamerPublicationViews.kt`
- `app/build.gradle.kts`
- `INSTRUCCIONES_AGENTES.md`
- `AGENTS.md`
- `docs/coach-audit-1.1.10.174.md`
