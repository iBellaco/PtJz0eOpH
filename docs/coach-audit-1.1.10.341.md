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
- Nueva matriz de 44 escenarios con coordenadas medidas independientemente en la
  imagen, identificación de los veinte retratos y confirmación de los diez baneos.
- La matriz utiliza el límite real de captura de 1920 × 1080 y los perfiles por
  fabricante/modelo; registra dimensiones nativas simuladas y dimensiones de captura.
- Evidencia JSON y PNG por perfil conservada en el flujo del escáner.
- La nueva prueba detectó asignaciones incorrectas de baneos en tablets 4:3 con
  bandas: el seguimiento calculaba la separación con el alto total de pantalla,
  aunque el detector ya había quitado las bandas. Ahora el seguimiento usa el
  alto del contenido real. El reconocimiento de identidades no cambia.
- Diez escenarios adicionales reconstruyen los paneles laterales anclados a los
  bordes, con avatares circulares a escala del alto, sin bandas. En pantalla 4:3
  el baneo interior queda fuera del 27% fijo usado anteriormente; se adapta el
  ancho de búsqueda al alto y la proporción en pantallas menores que 1.75:1.
- Cuatro perfiles de plegables comprueban áreas casi cuadradas. Se acepta el área
  observada del juego aunque su alto ocupe menos de media captura; se conservan
  los límites de búsqueda y el rechazo de fotogramas vacíos.
- La siguiente pasada aprobó todos los escenarios con bandas y plegables; dos
  escenarios 16:10 sin bandas detectaron un vecino texturizado que desplazaba el
  quinto baneo. Se completan los vecinos visibles antes de elegir las cinco
  posiciones exteriores. Se conserva el umbral de reconocimiento original.
- La búsqueda de vecinos adicionales se limita a las franjas ampliadas. Las
  filas panorámicas de Poco y Red Magic conservan su límite anterior para que
  indicadores fuera de los cinco baneos no desplacen un baneo real.

## Modelos y escenarios

| Perfil | Dimensiones de pantalla simuladas | Tipo de evidencia |
| --- | --- | --- |
| Red Magic / nubia NX789J | 2688 × 1216 | Referencia y simulación; funcionamiento físico reportado por el propietario |
| Poco X7 Pro / 2412DPC0AG | 2712 × 1220 | Simulación con perfil medido; funcionamiento físico reportado por el propietario |
| Familia S26 | 2340 × 1080, 2400 × 1080, 3120 × 1440, 3200 × 1440 | Escenarios simulados; no se certifica el panel ni hardware del equipo |
| Teléfonos genéricos | 20 configuraciones entre 640 × 360 y 3840 × 2160 | Simulación con imagen real |
| Tablets genéricas | 1024 × 768, 1600 × 1000, 2048 × 1536, 2560 × 1600 | Simulación con bandas de ajuste |
| Plegables genéricos | 1920 × 1792, 2208 × 1840, 2440 × 2268, 1800 × 1800 | Simulación con bandas y paneles sin bandas |

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
- `app/src/main/java/com/example/service/screen/BanDetection.kt`
- `app/src/main/java/com/example/service/screen/LiteRTVisionClassifier.kt`
- `app/src/main/java/com/example/service/screen/BannedPortraitScanner.kt`
- `app/src/main/java/com/example/service/screen/DraftFrameGeometry.kt`
- `app/src/test/java/com/example/DraftMultiDeviceAuditTest.kt`
- `app/src/test/java/com/example/BannedPortraitScannerTest.kt`
- `.github/workflows/build-apk.yml`
- `app/build.gradle.kts`
- Este reporte.
