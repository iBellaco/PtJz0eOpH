# Compatibilidad de dispositivos y requisitos de Coach

Versión documentada: Coach 1.1.10.404, build 1120.

Este documento distingue entre lo que exige el APK, lo que necesita el overlay y
lo que está comprobado en un teléfono físico. Una marca, una resolución o una
prueba de imagen no certifica todos los modelos de esa marca.

## Estado de dispositivos

| Fabricante y modelo | Android físico registrado | Aplicación | Overlay y escáner | Evidencia |
|---|---:|---|---|---|
| ZTE / nubia RedMagic 10 (NX789J) | No registrado en el informe | Confirmada por el propietario | Confirmados por el propietario | Prueba física reportada; la nueva geometría requiere repetir la prueba con la build actual |
| POCO (perfil reportado como POCO X7 Pro) | No registrado en el informe | Confirmada por el propietario | Confirmados por el propietario | Prueba física reportada; la nueva geometría requiere repetir la prueba con la build actual |
| Samsung Galaxy S26 | No disponible | No comprobada físicamente | No comprobado físicamente | Solo imágenes/emulador; no equivale a un teléfono real |
| Honor V2 | No registrado | Instalación observada | Overlay bloqueado por “Controlado por ajustes restringidos” | Pendiente de permitir ajustes restringidos desde Android; Coach no puede concederlo automáticamente |

Las pruebas Android instaladas del flujo son emulador y las pruebas de tamaños
son imágenes sintéticas. No cuentan como pruebas físicas de Samsung, Xiaomi,
Redmi, POCO, Honor ni de cualquier otra marca.

## Compatibilidad del APK

### Mínima técnica

- Android 7.0 o posterior (API 24), según `minSdk`.
- Arquitectura `armeabi-v7a`, `arm64-v8a` o `x86_64`.
- Espacio suficiente para instalar el APK y sus datos.
- Para iniciar sesión y sincronizar contenido: conexión a Internet y servicios
  compatibles con la instalación.

API 24 permite instalar la aplicación, pero no garantiza que Wild Rift, el
fabricante o todas las funciones actuales sean compatibles en ese teléfono.

### Recomendada

- Android 14, 15 o 16 (API 34–36).
- Arquitectura `arm64-v8a`.
- Al menos 4 GB de RAM; 6 GB o más es preferible para jugar y mantener el
  reconocimiento activo.
- GPU compatible con aceleración de Android, almacenamiento libre y batería sin
  restricciones agresivas.
- Wild Rift instalado desde una fuente compatible con el dispositivo.

## Compatibilidad del overlay y del escáner

### Mínima técnica

- Android 7.0/API 24 para instalar el APK.
- Permiso del sistema “Mostrar encima de otras aplicaciones”
  (`SYSTEM_ALERT_WINDOW`).
- Permiso de captura de pantalla de Android (MediaProjection), concedido por el
  usuario cada vez que el sistema lo solicite.
- El sistema debe permitir el servicio en primer plano y sus notificaciones.
- Wild Rift debe poder ejecutarse en la misma sesión y la captura debe mostrar
  sus slots de selección.

En Android 13 o posterior se recomienda conceder también las notificaciones. En
Android 14 o posterior las restricciones del servicio en primer plano y de
MediaProjection pueden impedir el escáner aunque el APK esté instalado.

### Recomendada

- Android 14–16/API 34–36 en un dispositivo arm64.
- 6 GB de RAM o más y un chipset capaz de ejecutar Wild Rift y OCR sin presión
  térmica excesiva.
- Permiso de overlay activo, permiso de captura vigente y Coach excluido de las
  restricciones de batería del fabricante.
- Orientación y ventana de juego estables; no cambiar escala, modo flotante o
  densidad durante una partida.
- Para los primeros nueve picks, títulos completos visibles; la décima selección
  necesita la confirmación visual condicionada definida en el contrato del
  escáner.

El overlay no requiere coordenadas guardadas por marca: usa la geometría
normalizada y la calibración adaptativa compartida. Las coordenadas enviadas al
soporte son diagnósticos de una sesión, no una certificación automática del
modelo.

## Marcas y modelos solicitados

El catálogo de modelos solicitado está conservado en
`docs/scanner-requested-models.json`. Sus entradas son una lista de objetivos de
compatibilidad, no una lista de equipos aprobados. Para marcar un modelo como
compatible hacen falta el modelo exacto, la versión de Android, resolución,
densidad, orientación y una prueba física reproducible de overlay y escáner.

No se hereda la aprobación entre Samsung, Xiaomi/Redmi/POCO, Motorola, Huawei,
Honor, OPPO, realme, vivo/iQOO, OnePlus, Google, Sony, ASUS, Nokia, ZTE/nubia,
Tecno, Infinix, TCL, Nothing, Meizu, Sharp, HTC, LG, Fairphone o marcas
rugerizadas. Apple/iOS queda fuera porque este proyecto es un APK Android.

## Cómo completar un registro físico

Para cada equipo se debe registrar:

1. Modelo exacto y versión de Android/API.
2. Resolución, densidad y orientación efectiva de la captura.
3. Permisos de overlay, captura y batería.
4. Tres capturas de baneos, diez selecciones, latencia observada, reinicio y
   persistencia de calibración.
5. Resultado del visor y del escáner por separado.

Guardar cada resultado en `docs/device-audits/` como
`<modelo>-<version>-<fecha>.md`. Hasta que exista ese informe, el estado debe
permanecer como `Pendiente`.

