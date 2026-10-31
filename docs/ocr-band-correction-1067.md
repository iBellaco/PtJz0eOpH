# Corrección de banda OCR — build 1067

El recorte fijo del 24 % introducido en 6030815f podía eliminar letras del
título aliado. Se sustituye por la alternativa existente con supresión de
insignia, conservando íntegramente el ancho y las coordenadas de origen.

Se retiran las equivalencias IA/IAA/rVI a Vi introducidas en 2f88647e.
Las regresiones comprueban conservación del ancho y rechazo de esas lecturas.

Validación pendiente: ejecución nativa de 1920×864, 1920×924 y 640×360.
No se considera resuelto el reconocimiento hasta aprobar esas pruebas.
Se conserva la versión 1.1.10.351 build 1067 todavía no publicada.
