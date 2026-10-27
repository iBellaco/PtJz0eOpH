# Coach 1.1.10.392 — build 1108

## Hallazgos

- Red Magic ya partía de geometría compartida en 391; el propietario informa
  funcionamiento físico, con demora al trasladar la selección al Hub.
- POCO X7 Pro todavía seleccionaba un preset por modelo e instalaba ese preset
  al cargar preferencias. Se elimina esa excepción. La geometría normalizada
  compartida sigue siendo necesaria para localizar títulos, slots y baneos.
- Recuperar títulos no reconocidos hacía hasta cuatro solicitudes OCR
  consecutivas por slot en la etapa de cajas medidas. Es una fuente de trabajo
  adicional, no una medición física que explique por sí sola toda la demora.
- La captura del Honor V2 indica «Controlado por ajustes restringidos»: bloqueo
  de Android. No es una coordenada incorrecta ni un permiso otorgable por Coach.

## Cambios

1. Defaults iguales para todos los modelos. Migración una sola vez de geometría
   antigua POCO (global/India/nombre comercial), conservando ajustes ajenos y
   manuales nuevos. Red Magic conserva su marcador previo, sin borrar otra vez
   los ajustes que el usuario hubiera guardado tras su migración anterior.
2. Recuperación OCR por lotes de etapa/candidato, con píxeles y límites de título
   intactos. Se conservan dos candidatos, tamaños nativos y dos fotogramas para
   confirmar. No se rellenan picks 1–9 mediante retratos. La evidencia secuencial
   incluye cantidad de solicitudes OCR junto al tiempo ya registrado.
3. Guía condicional en español/portugués, botón a Información de la app, controles
   de al menos 48dp, diálogo desplazable. Aviso de confiar en el APK; si la opción
   falta o la política del dispositivo lo impide, no se promete desbloqueo.
   Intents con alternativas seguras para ajustes ausentes o bloqueados en OEM.
4. Regresión POCO instalada comienza con geometría común, sin reinstalar el
   preset eliminado. Se ejecuta una vez junto a las dos regresiones Red Magic
   en el grupo preliminar, no tres veces en toda la matriz.

## Verificación y límites

Pruebas añadidas: migración POCO y defaults multimarcas; empaquetado píxel a píxel
de diez slots, incluido tamaño nativo; destinos/fallbacks de permisos y traducción.
Fix-error incluye esas pruebas, alineación de anillos y cobertura de 142 nombres;
conserva los seis casos secuenciales aislados previos en debug/release.

Verificación local: compilación Kotlin debug correcta y 50 pruebas Robolectric
aprobadas (2 cobertura del catálogo, 5 perfiles/migración, 29 atlas de títulos,
9 alineación y 5 permisos/idioma), sin fallos ni omisiones. Los tests de catálogo
exigen 142 nombres exactos. También pasan 13 pruebas Python de secuencia/informes,
2 de cobertura de dispositivos, 2 de contenido y la validación del catálogo;
XML es/pt, YAML y `git diff --check` correctos. Se utilizó el SDK temporal ya
existente y el JDK completo: el Java predeterminado carecía de compilador.
`:app:compileDebugAndroidTestKotlin` también completó correctamente; compilar
las pruebas instrumentadas no equivale a ejecutarlas.

OCR nativo en emulador y compilación release quedan pendientes de Actions.
No se afirma menor latencia física ni compatibilidad física del Honor o POCO
con 392 antes de probarlos. Imágenes
reales y redimensionadas instaladas en emulador siguen siendo pruebas de imágenes,
no pruebas de teléfonos físicos. No se toca main ni se fusiona automáticamente.
No se crea vigilancia periódica.

Archivos: VisionCalibrationConfig.kt, DraftVisionScanner.kt, SystemPermissionHelper.kt,
OverlayPermissionHelp.kt, MainDraftingScreen.kt, BatteryAndOverlayNoticeCard.kt,
strings.xml (es/pt), DeviceDraftProfileTest.kt, DraftNameAtlasTest.kt,
SystemPermissionHelperTest.kt, DraftRivalNameInstalledTest.kt,
DraftSequenceMultiDeviceInstalledTest.kt, verify-targeted-red-magic.py,
fix-error.yml, app/build.gradle.kts, draft-recognition-contract.md y este informe.
