# Coach 1.1.10.394 — build 1110

La validación 40414178869 (cc9ed712) aprobó core-and-rendered, incluidas las
traducciones corregidas en 393. Aprobaron también compilación, permisos, auditorías
instaladas y 15/16 grupos de secuencia. El grupo 6, trabajo 121907335598, falló
instalando la imagen Android 34, antes de ejecutar OCR:

`Error reading Zip content from a SeekableByteChannel.`

sdkmanager salió con código 1. El emulador no llegó a iniciarse. La ausencia de
capturas hizo fallar la subida de evidencias; la auditoría agregada, entrega y
reporte final fallaron por ese grupo ausente. El verde anterior del mismo grupo
no garantiza que una descarga nueva desde otro runner vaya a completarse.

Se preinstala la imagen exacta antes de cada emulador, tanto en fix-error como en
validación. Solo los errores conocidos de descarga/descompresión y timeout permiten
hasta tres intentos, con espera de 5 y 10 segundos. Un error de configuración
falla inmediatamente. Se conserva la salida de cada intento en los logs. Si no
se instala la imagen, el trabajo permanece fallido; las pruebas OCR nunca se
reintentan ni se marcan aprobadas por esta recuperación. Los requisitos de
evidencia y publicación siguen siendo obligatorios.

Cinco regresiones automatizadas cubren el error exacto seguido de éxito, fallo
persistente, configuración inválida, timeout y éxito inicial. Simulan respuestas
de sdkmanager para probar la política de reintentos; no son OCR ni teléfonos
físicos. La instalación real y el OCR en emulador deben aprobar en el nuevo
flujo de fix-error antes de promover la corrección.

Archivos: tools/prepare-emulator-image.py, tools/test_emulator_image.py,
.github/actions/prepare-emulator/action.yml, fix-error.yml, build-apk.yml,
app/build.gradle.kts y este informe. No se cambia código de reconocimiento.
