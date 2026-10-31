# Auditoría Coach 1.1.10.267 (build 983)

## Problema revisado

El gestor de mensajes consultaba únicamente `privateMessages` y la subcolección `messages`, mientras que la bandeja también mostraba tickets de soporte. Esto permitía que el gestor indicara cero mensajes aunque la bandeja todavía tuviera solicitudes visibles.

## Cambios

- El gestor unifica mensajes directos y tickets de soporte usando los mismos datos visibles en la bandeja.
- Se conserva la deduplicación por identificador y se muestra el contenido de los tickets con su fecha y estado de lectura.
- El borrado desde el gestor elimina el ticket, sus copias directas y la referencia heredada en una sola transacción.
- Los tickets históricos que no tengan `userId` se validan también por el correo de la cuenta antes de eliminarlos.
- Se verificó que los botones del gestor y de la bandeja ya usan los componentes de pulsación animada global; no se añadió una animación nueva.

## Archivos modificados

- `app/src/main/java/com/example/data/UserMessageDeleteRepository.kt`
- `app/src/main/java/com/example/ui/components/AdminPrivateMessageDialog.kt`
- `app/src/test/java/com/example/InboxSynchronizationTest.kt`
- `app/src/main/assets/translations_pt.json`
- `app/build.gradle.kts`

## Verificación

- Prueba unitaria añadida para la asociación segura de tickets por identificador y correo.
- `git diff --check` ejecutado sin errores.
- La compilación local no pudo iniciar porque el entorno no tiene disponible la descarga de Gradle; la validación final se ejecutará en GitHub Actions antes de fusionar.
