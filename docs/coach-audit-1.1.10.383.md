# Coach 1.1.10.383 — build 1099

La ejecución 40268260375 aprobó las pruebas unitarias pero omitió VI en
1920×822, 1920×924 y 1920×889. Teemo quedó bloqueado correctamente.
El recorte corto guardado todavía muestra un trazo unido a V: la supresión
geométrica del emblema no lo elimina y la proyección conjunta blanco/dorado
lo conserva. La corrección anterior no está aprobada.

Se incorpora una alternativa independiente de tinta neutra sobre los píxeles
originales: descarta colores con diferencia de canales superior a 40 o un
canal mínimo menor que 90. Mantiene las alternativas anteriores para títulos
dorados y no convierte NI en alias ni rellena títulos por retratos. Las nuevas
variantes se agrupan con la solicitud OCR de títulos cortos existente.

La prueba real de las tres geometrías exige conservar tinta neutra dentro de
la banda. La instrumentación guarda recortes neutral-title y su lectura para
verificar el resultado real. Se conserva la cobertura obligatoria de 142 nombres.
Archivos: DraftNameAtlas.kt, DraftVisionScanner.kt, DraftNameAtlasTest.kt,
DraftSequenceMultiDeviceInstalledTest.kt, app/build.gradle.kts y este informe.

Verificación local: 9/9 pruebas Python y diff limpio. Sin SDK local; Kotlin y
reconocimiento instalado pendientes de Actions. Son imágenes sobre emulador,
no pruebas físicas de esos modelos. No se modifica main ni coach-validacion.
