# Coach 1.1.10.490 — Calibración unificada, patrocinadores y favoritos por línea

## Diagnóstico

- La calibración mostraba dos acciones separadas: una guardaba en el dispositivo
  y otra enviaba un informe. El flujo de producción podía dejar guardada una
  coordenada sin enviar el reporte, o impedir que el administrador recibiera
  una notificación visible.
- Las acciones administrativas de patrocinadores podían usar un token de
  Firebase desactualizado después de cambiar o confirmar el permiso de
  administración. Eso desviaba una decisión válida a la cola de recuperación y
  mantenía el botón cargando.
- El filtro de favoritos se desactivaba silenciosamente cuando había menos de
  cinco campeones en la línea, sin explicar el requisito. Tampoco describía
  cuándo todos los favoritos eran desfavorables frente al rival.

## Cambios implementados

- `Guardar y enviar coordenadas` reemplaza el uso de dos botones en los paneles
  de calibración. La coordenada se persiste primero para el dispositivo y luego
  se registra en `support_reports` como informe privado para administración,
  incluyendo usuario, modelo, resolución, captura y coordenadas.
- Los informes de calibración del propio administrador generan también el evento
  de soporte del panel administrativo, sin copiar el contenido a la bandeja
  personal ni hacerlo visible para moderadores.
- Las operaciones de patrocinadores y otros comandos privilegiados refrescan una
  sola vez el token firmado cuando el permiso puede estar desactualizado. El
  panel de patrocinio refresca la autorización únicamente al abrirse; no se
  añadió monitoreo continuo.
- El formulario del administrador ya no se bloquea por una lectura local de
  saldo todavía desactualizada ni por la comprobación visual de solapamiento;
  el servicio económico sigue siendo la autoridad y valida saldo, costo e
  identificador de forma atómica antes de descontar.
- Las recomendaciones de favoritos se mantienen limitadas al catálogo personal
  únicamente cuando existen cinco favoritos válidos para la línea. Con menos de
  cinco, el interruptor explica el requisito y muestra un aviso al intentar
  activarlo. Si todos los favoritos son desfavorables, la lista elige la opción
  menos desfavorable según la composición y expone ese motivo en la explicación.
- Se incrementó la versión de producción a `1.1.10.490`.

## Verificaciones

- Se añadieron regresiones para el requisito de cinco favoritos y la detección
  de informes de calibración privados.
- La integración económica cubre también la creación y publicación de un
  anuncio con una cuenta administrativa sin rol secundario de patrocinador.
- Se revisaron las pruebas existentes de revisión de patrocinadores, cola
  económica y reglas de Firestore.
- `git diff --check` y las comprobaciones de catálogos/localización quedan
  preparadas para el flujo selectivo de `fix-error`.
- La compilación Android release y las pruebas instrumentadas se ejecutan en
  GitHub Actions, donde está disponible el SDK y el emulador de Android.

## Alcance de operación

No se añadió monitoreo permanente. La actualización del permiso se realiza una
vez al entrar al panel y las decisiones económicas siguen siendo idempotentes,
con recuperación duradera únicamente cuando el servicio directo no está
disponible.
