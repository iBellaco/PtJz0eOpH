# Auditoría Coach 1.1.10.341 — build 1057

## Alcance

Comprobar nuevamente los diez slots y diez baneos de la captura de referencia del
propietario en el camino real de captura y calibración. La imagen recibida tiene
SHA-256 `42661aefae3df52b4f72047c039120f72f4ea351139d7bdb5107351fe8d3342a`,
idéntico a la referencia de regresión guardada en el proyecto.

Las imágenes se adaptan con escalado uniforme y bandas exteriores para conservar
la forma circular de los avatares. Estirar una imagen de 20:9 a 4:3 deforma sus
avatares y no reproduce el escalado uniforme de la captura Android.

## Cambios

- El visor dibuja los círculos desde el mismo rectángulo de píxeles que extrae el
  escáner, incluido el mínimo de 32 píxeles, redondeo y límite de bordes. Antes
  dibujaba directamente desde porcentajes y podía diferir del recorte efectivo.
- Nueva matriz de 30 perfiles con coordenadas medidas independientemente en la
  imagen, identificación de los veinte retratos y confirmación de los diez baneos.
- La matriz utiliza el límite real de captura de 1920 × 1080 y los perfiles por
  fabricante/modelo; registra dimensiones nativas simuladas y dimensiones de captura.
- Evidencia JSON y PNG por perfil conservada en el flujo del escáner.

## Modelos y escenarios

| Perfil | Dimensiones de pantalla simuladas | Tipo de evidencia |
| --- | --- | --- |
| Red Magic / nubia NX789J | 2688 × 1216 | Referencia y simulación; funcionamiento físico reportado por el propietario |
| Poco X7 Pro / 2412DPC0AG | 2712 × 1220 | Simulación con perfil medido; funcionamiento físico reportado por el propietario |
| Familia S26 | 2340 × 1080, 2400 × 1080, 3120 × 1440, 3200 × 1440 | Escenarios simulados; no se certifica el panel ni hardware del equipo |
| Teléfonos genéricos | 20 configuraciones entre 640 × 360 y 3840 × 2160 | Simulación con imagen real |
| Tablets genéricas | 1024 × 768, 1600 × 1000, 2048 × 1536, 2560 × 1600 | Simulación con bandas de ajuste |

Las pruebas existentes también cambian el arte central y permutan los retratos,
comprueban perfiles persistentes y una matriz geométrica densa. Ninguna simulación
se presenta como una prueba realizada en un teléfono físico.

## Criterios y verificación

- Diez identidades correctas por slot con los umbrales habituales sin reducirlos.
- Diez identidades correctas por baneo, posición y estado confirmado del visor.
- Error máximo de centro de seis píxeles referido a la imagen original 1280 × 579.
- Captura, configuración del visor y configuración del escáner coincidentes.
- No se puede enumerar cada dimensión posible de Android: la app no tiene una lista
  finita de tamaños. La matriz comprueba representantes y transformaciones, no
  garantiza toda skin, firmware o futura distribución del juego.
- La ejecución local de Gradle no pudo descargar su distribución. Actions debe
  ejecutar la matriz, pruebas completas, instalación y firma antes de fusionar.

## Archivos

- `app/src/main/java/com/example/ui/components/ScannerDebugOverlay.kt`
- `app/src/test/java/com/example/DraftMultiDeviceAuditTest.kt`
- `.github/workflows/build-apk.yml`
- `app/build.gradle.kts`
- Este reporte.
