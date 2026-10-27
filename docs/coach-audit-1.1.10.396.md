# Coach 1.1.10.396 — build 1112

Se identificó una ruta reproducible del mensaje genérico al publicar publicidad:
si el servicio callable no está disponible, EconomyServiceClient bloqueaba la
cola para cualquier cuenta con claim admin, incluido su anuncio personal.
La captura no permite confirmar la respuesta concreta del servicio productivo.

Se permite exclusivamente SPONSOR como respaldo para esas cuentas. El trabajador
sigue comprobando sesión, rol, saldo y precio; no se habilita escritura directa
de saldos ni anuncios. Las operaciones administrativas siguen bloqueadas en este
respaldo. operationId y payload.id conservan el identificador reservado de la
llamada directa, incluso en reintentos, para impedir un segundo cargo.

Se añade regresión de la política a EconomyRequestPolicyTest y al gate aislado
fix-error. El resultado remoto debe verificarse antes de promover esta revisión.
git diff --check pasó. La prueba local de Gradle no pudo arrancar: SDK location
not found; este entorno no dispone del SDK Android configurado.
No se verificó publicación física desde un teléfono ni se publicaron anuncios
de prueba en producción.
