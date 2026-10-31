# Coach 1.1.10.390 — build 1106

## Fallo comprobado

Ejecución 40311314615, revisión c427137c: el trabajo debug aprobó, incluidos
los 142 nombres y los casos secuenciales 42 y 52. En release aprobaron 49
(1920×822) y 56 (1920×924). Falló 63 (1920×889): en draft-mel-nine.jpg
faltó el título Vi del aliado 4; Teemo permaneció correctamente bloqueado.

El diagnóstico nativo registró `SENNA VI SENNA`; el elemento `VI` estaba en
Rect(122,32–148,48), dentro del objetivo Rect(118,28–152,52). Sin embargo,
producción elegía solamente el contexto con mayor relación ancho/alto de los
recortes anteriores. El diagnóstico usaba la caja de la escena original
Rect(202,153–253,165). Es evidencia de que ese segundo camino sí produjo texto,
no aprobación de la nueva integración.

## Corrección

Tras la lectura de escena, los aliados aún sin título pueden usar hasta tres
contextos de otros slots, procedentes de cajas nativas de títulos completos
reconocidos. Orden espacial estable; sin preferencia por el nombre más ancho.
Se conserva el intento anterior que aprobó los otros dos casos. Se detiene
al resolver el título. Solo se acepta el texto contenido en el slot objetivo;
los donantes no aportan su identidad. No se modifican umbrales, confirmaciones,
requisitos de nueve nombres, etiquetas esperadas ni plazos de prueba.

Archivos: DraftVisionScanner.kt, DraftNameAtlas.kt, DraftNameAtlasTest.kt,
app/build.gradle.kts y este informe.

## Verificación y límites

Se añadieron regresiones para límite de tres contextos, exclusión del propio
slot y líneas fuera de la banda, y proyección de la caja nativa del fallo real.
La prueba instalada existente sigue exigiendo todos los títulos y Teemo.
Verificación local: 9 pruebas Python y `git diff --check`; las nuevas pruebas
Kotlin y el OCR nativo requieren Actions porque aquí no hay SDK/emulador.
No se declara solucionado hasta que ese flujo apruebe. Son pruebas de imágenes
en emulador, no físicas ni prueba de firma productiva. Main y la solicitud #163
no se modifican. La vigilancia automática permanece pausada.
