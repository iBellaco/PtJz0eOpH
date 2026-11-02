# Coach 1.1.10.448 — build 1164

## Alcance

Publicar la aclaración documental solicitada sobre los puestos. La corrección
funcional de escalado de 1.1.10.447 ya pasó fix-error (ejecución 41377803675).
Esta entrega conserva ese código y la base acumulada de validación.

AGENTS.md exige leer la secuencia del contrato: rivales estáticos con viewport
constante; aliados desplazables individualmente; avatar con título de línea
como preselección; título de campeón como selección con confirmación temporal.
Se explicita que no se deben mover las cinco filas por el desplazamiento de una.

No se presenta el seguimiento individual como implementado: DraftRingAlignment
y DraftGeometryStability todavía ajustan columnas. Adaptarlos al contrato y
probar secuencias mixtas de estados queda pendiente de una intervención funcional.

## Archivos y comprobación

AGENTS.md, docs/draft-recognition-contract.md, docs/coach-audit-1.1.10.447.md,
este informe y app/build.gradle.kts (identificador de entrega).
Revisado el diff; no se cambian algoritmos, recursos ni permisos.
Se publica una vez finalizada la ejecución anterior, sin cancelarla. La nueva
ejecución queda pendiente; no se genera aquí un APK final ni se afirma una
comprobación física de la nueva secuencia.
