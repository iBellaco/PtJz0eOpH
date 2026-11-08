# Coach 1.1.10.493 — Moderación de patrocinio, soporte y calibración de baneos

## Diagnóstico

- La aprobación o el rechazo de un anuncio esperaba primero una función remota
  opcional. Cuando ese servicio no estaba publicado, el botón permanecía
  cargando y terminaba mostrando una decisión sin confirmar.
- El panel de reportes se componía detrás de la ventana de moderación, por lo
  que su primera apertura quedaba parcialmente oculta en el borde inferior.
- El informe de coordenadas podía usar el prefijo del correo cuando no tenía un
  nombre cargado y llegaba sin una categoría específica en la bandeja
  administrativa.
- La calibración de baneos movía los cinco retratos como una sola fila y no
  conservaba posición ni tamaño independientes para cada baneo.

## Corrección

- Una solicitud de patrocinio ya creada se aprueba o rechaza con una única
  transacción administrativa. La publicación se actualiza inmediatamente y el
  rechazo deja, en la misma transacción, la orden privada e idempotente que
  verifica y devuelve el cargo original.
- La publicación pública del patrocinio excluye correo, identidad interna y
  campos contables.
- El panel de reportes se abre por encima de la ventana de moderación desde el
  primer intento.
- El informe de calibración obtiene el nombre público guardado en Coach, nunca
  deriva el nombre del correo, presenta dispositivo, pantalla, captura, versión
  y coordenadas en secciones legibles, y aparece con la etiqueta administrativa
  `COORDENADAS`.
- La calibración permite mover y redimensionar la fila completa de baneos de
  cada equipo o cualquiera de sus cinco espacios por separado. Los valores
  individuales se guardan por dispositivo y perfil de captura; la migración
  conserva las filas calibradas con versiones anteriores.
- Se incrementó la versión de producción a `1.1.10.493` (`1209`).

## Verificación local

- Prueba específica de patrocinio en el emulador de Firestore: correcta para
  aprobación, rechazo, publicación atómica, permisos e idempotencia.
- Suite completa de reglas y servicios: 65 escenarios de Firestore, 31 casos de
  integración económica y los ejercicios de eliminación, recuperación y
  reconciliación terminaron correctamente.
- Servicio económico: 52 pruebas unitarias correctas.
- Compilación Kotlin de la aplicación y de sus pruebas: correcta con Android 36
  y Java 21.
- Pruebas dirigidas de patrocinio, apertura de soporte, informe de coordenadas,
  calibración renderizada y lectura de baneos: correctas.
- Recursos localizados, catálogo generado, auditoría portuguesa y contenido del
  parche 7.3e: correctos.
- `git diff --check`: correcto.
- Las auditorías en dispositivo siguen siendo obligatorias en la validación
  selectiva de `fix-error` antes de integrar esta versión en
  `coach-validacion`.

## Alcance de operación

No se publica ni fusiona en `main`. No se añade monitoreo continuo. Cada paso a
validación depende de que la ejecución completa de la rama anterior termine en
verde.
