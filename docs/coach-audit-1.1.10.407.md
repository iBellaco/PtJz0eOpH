# Coach 1.1.10.407 — build 1123

## Fallo observado

En la ejecución 40655659286, las pruebas JVM y recursos pasaron. El trabajo
122705943766 falló al leer una solicitud como administrador: la prueba aislada
creaba el token con privilegios pero no el perfil users/admin requerido por
activeStaffAccount. El servicio rechazó la lectura antes de aprobar el anuncio.

## Corrección

- tests/firestore/sponsor-review.test.mjs prepara los perfiles ficticios del
  administrador activo, patrocinador, tercero y administrador suspendido.
- Se verifica que un token administrativo sin perfil o con perfil suspendido no
  pueda aprobar y que el anuncio permanezca pendiente tras esos intentos.
- Se mantienen las pruebas de aprobación atómica, rechazo, aprobación repetida,
  reembolso y conservación de avisos e intervalos.
- app/build.gradle.kts incrementa versión y build.

No se modifican reglas de acceso ni cuentas reales. La nueva ejecución queda en
fix-error; el propietario avisará del resultado antes de pasar a validación.

## Verificación local

La prueba sponsor-review.test.mjs pasó en el emulador con las reglas reales
(salida 0): propiedad del anuncio, administrador activo, aprobación atómica,
rechazo, operación repetida y protección de anuncios reembolsados. Los mensajes
PERMISSION_DENIED de los casos negativos son rechazos esperados por la prueba.
También pasaron node --check y git diff --check.
