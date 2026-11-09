# Coach 1.1.10.495 — Entrega pública de anuncios patrocinados

## Diagnóstico

- La aprobación actualizaba una lista compartida dentro de un único documento.
  Aunque la decisión quedaba confirmada, una escritura concurrente o una copia
  antigua de esa lista podía retirar el anuncio antes de que Inicio lo pintara.
- La pantalla dependía únicamente de esa lista y no tenía una fuente pública
  independiente por anuncio para recuperar la publicación aprobada.

## Corrección

- Cada aprobación escribe atómicamente una copia saneada en
  `app_notices/{id}`, sin correo, identificadores privados ni datos contables.
- Inicio escucha esa colección pública en tiempo real y la combina por ID con
  los avisos generales; la copia pública prevalece sobre cualquier estado local
  pendiente.
- Una aprobación repetida repara el documento público si faltaba, y un rechazo
  elimina tanto la entrada de la lista como el documento público.
- El servicio económico y la ruta administrativa directa aplican exactamente
  la misma publicación y retirada.
- Se incrementó la versión de producción a `1.1.10.495` (`1211`).

## Verificación

- Regresión de Android para reemplazo de caché pendiente, publicación visible
  y retirada al borrar el documento público.
- Prueba de reglas para lectura pública del anuncio saneado y eliminación tras
  rechazo.
- Integración del servicio económico para aprobación directa, aprobación desde
  cola y rechazo.

## Alcance

La corrección se envía únicamente a `fix-error`. No se fusiona en `main` ni se
inicia monitoreo continuo desde este cambio.
