# Coach 1.1.10.398 — build 1114

Ejecución 40523663634, trabajo 122257325328: la prueba instalada
reportedSlotsDoNotLoseSennaOrMalphiteAndFinishWithVisualRivalPick falló con
expected:<malphite> but was:<null> al consultar alliesByRole[TOP]. Las aserciones
previas de identidad en alliesBySlot sí pasaron. La captura inicia después de
seleccionar esos jugadores y no aporta las líneas previas. El contrato exige
conservar la línea observada, no inferirla por afinidad del campeón.

La regresión ahora conserva las comprobaciones de los cuatro nombres, el slot
vacío, los nueve títulos y el décimo visual rival Fiddlesticks. Para Malphite y
Senna exige coherencia con una línea observada si existe y prohíbe asignar una
por afinidad si no existe. No se restauran las asignaciones especulativas.

La ejecución anterior aprobó OCR aislado y ambas secuencias completas en release
(Red Magic y Xiaomi). Son pruebas con imágenes/emulador, no teléfonos físicos.
git diff --check y auditoría Python local se ejecutan; resultado de la prueba
instalada actual pendiente en fix-error. No se modifica main.
