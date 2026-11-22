# Coach 1.1.10.585 (1301) · Desplazamiento fluido en soporte

## Problema corregido

El buzón de mensajes ejecutaba una animación infinita en el mismo nivel de
composición que la lista. Cada cambio de la animación invalidaba el contenedor
completo mientras el usuario desplazaba los mensajes, provocando tirones y
sensación de bloqueo. El hilo de soporte, además, tenía un desplazamiento
vertical sin un viewport acotado de forma explícita.

## Cambios realizados

- Se eliminó el pulso continuo del contenedor de `UserInboxDialog` y se dejó el
  indicador de mensaje nuevo con un estilo estático de alto contraste.
- Se fijó la lista principal del buzón al espacio restante mediante `weight(1f)`
  y se añadió una etiqueta de prueba estable para el desplazamiento.
- Se reutiliza el formateador de fechas del buzón en lugar de crear uno por
  elemento durante la composición.
- Se acotó el hilo de soporte al espacio disponible, con un único estado de
  desplazamiento y una etiqueta de prueba propia.
- Se aisló el pulso del icono del buzón de reportes en un composable hijo para
  que su animación no invalide la lista de reportes.
- Se acotó también la lista de mensajes privados del visor de usuario al espacio
  restante y se reutiliza su formateador de fechas para evitar trabajo durante
  el desplazamiento.

## Archivos modificados

- `app/src/main/java/com/example/ui/components/UserInboxDialog.kt`
- `app/src/main/java/com/example/ui/components/AdminSupportReportsDialog.kt`
- `app/src/main/java/com/example/ui/components/AdminPrivateMessageDialog.kt`
- `app/build.gradle.kts`

## Verificaciones

- `git diff --check` sin errores.
- `:app:testDebugUnitTest --tests com.example.PortugueseRenderedAuditTest`
  completado correctamente, incluyendo las superficies de buzón y soporte.
- Auditoría de portugués sin hallazgos de texto español.
- La compilación release local no pudo iniciarse porque el entorno no dispone
  del keystore ni de las variables protegidas de firma de producción; no se
  utilizó una firma de depuración.
