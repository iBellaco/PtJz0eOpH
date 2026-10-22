# Recuperación de Coach

El workflow de cuentas consulta, sin modificar datos, la programación de copias,
las copias listas y la configuración de recuperación temporal. El resultado
está en el resumen de Actions y en `recovery-inventory/backup-status.json`.
UNKNOWN significa que faltan permisos o una consulta falló; no equivale a tener
copias. NONE solo se informa si la consulta fue autorizada y respondió vacía.
El inventario cubre documentos; no demuestra respaldo de identidades ni archivos.

## Ensayo automatizado

`npm test --prefix tests/firestore` ejecuta un ensayo de recuperación aislado:
crea documentos ficticios anidados, guarda una copia local con permisos 0600 y
SHA-256, elimina únicamente esa fixture, restaura y compara datos y tipos.
Comprueba timestamps, enteros grandes, bytes, mapas, arrays, referencias y
coordenadas. Rechaza cualquier destino que no sea el emulador loopback y el
proyecto de pruebas. No demuestra la restauración de una copia productiva.

## Antes de una migración o una recuperación real

1. Consultar el inventario reciente. Si es UNKNOWN, verificar los permisos de
   lectura; si es NONE, definir retención y almacenamiento antes de migrar.
   Este cambio no activa servicios de pago ni crea almacenamiento.
2. Comprobar que la copia está READY, pertenece al proyecto previsto y cubre el
   instante anterior al cambio. Registrar fecha, alcance y retención sin datos
   personales. Verificar por separado identidades y archivos subidos.
3. Restaurar primero en una base aislada autorizada. Comparar conteos, tipos,
   registros anidados, permisos y muestras funcionales antes de considerar la
   copia recuperable. No ejecutar importaciones sobre la base activa como ensayo.
4. Para recuperación real, detener temporalmente las operaciones afectadas,
   conservar una copia del estado actual y preparar el retorno. Tras restaurar,
   validar acceso, roles, compras, builds y adjuntos antes de reabrir operaciones.

## Limpiezas interrumpidas

La limpieza de cuentas no verificadas mantiene una cola privada antes de borrar
una identidad. Si falla la limpieza del perfil, informa error y conserva el
trabajo para la siguiente ejecución. La recuperación solo elimina el perfil
cuando confirma que ya no existe la identidad. No borra identidades que se hayan
verificado, iniciado sesión recientemente o adquirido roles/beneficios durante
el escaneo. Ninguna transacción puede hacer atómicas las dos APIs; las lecturas
finales reducen esa ventana y la cola permite recuperar fallos entre operaciones.
