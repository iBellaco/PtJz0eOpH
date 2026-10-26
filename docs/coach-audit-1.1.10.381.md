# Coach 1.1.10.381 — build 1097

La ejecución 40259066416 volvió a omitir VI en las tres capturas de regresión.
Sus artefactos incluyen por primera vez los recortes cortos exactos del fallo.
La inspección visual de sequence-49-enemy-first-draft-mel-nine.jpg-short-4.png
mostró las letras a la izquierda y un trazo del fondo muy alejado a la derecha:
el límite global de tinta trataba ambos como un solo título, reduciendo las letras.

La alternativa corta separa ahora el primer grupo de columnas de tinta cuando
encuentra un hueco vacío proporcional a la altura de la banda. Recalcula después
sus límites verticales. Se conserva la banda intacta en los intentos generales,
por lo que esta recuperación no reemplaza la lectura completa de nombres largos.
La regresión con las tres capturas reales comprueba que el recorte corto excluya
el trazo distante. No se amplían alias ni se rellenan nombres desde retratos.

Archivos: DraftNameAtlas.kt, DraftNameAtlasTest.kt, app/build.gradle.kts y este
informe. Se conservan las pruebas de los 142 nombres y los tres casos instalados
release en fix-error. El resultado nativo queda pendiente de Actions; las
imágenes sobre emulador no equivalen a pruebas físicas de esos dispositivos.
