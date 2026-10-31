# Recuperación de Coach

El workflow de cuentas consulta, sin modificar datos, la programación de copias,
las copias listas y la configuración de recuperación temporal. El resultado
está en el resumen de Actions y en `recovery-inventory/backup-status.json`.
UNKNOWN significa que faltan permisos o una consulta falló; no equivale a tener
copias. NONE solo se informa si la consulta fue autorizada y respondió vacía.
El inventario cubre documentos; no demuestra respaldo de identidades ni archivos.
Recorre las páginas de copias y programación; un fallo posterior no se presenta
como una lista completa. Si la API entrega el tamaño de la copia lista más reciente,
lo conserva como `latestReadyBackupBytes` para preparar la cotización.

## Ensayo automatizado

`npm test --prefix tests/firestore` ejecuta un ensayo de recuperación aislado:
crea documentos ficticios anidados, guarda una copia local con permisos 0600 y
SHA-256, elimina únicamente esa fixture, restaura y compara datos y tipos.
Comprueba timestamps, enteros grandes, bytes, mapas, arrays, referencias y
coordenadas. Rechaza cualquier destino que no sea el emulador loopback y el
proyecto de pruebas. No demuestra la restauración de una copia productiva.

El segundo ensayo (`recovery-media.integration.mjs`) copia a disco atributos de una
cuenta ficticia, su correo verificado, permisos personalizados, enlaces y adjuntos.
Elimina únicamente esa fixture, restaura y compara los bytes por SHA-256. Exige
que los tres emuladores estén en loopback. El informe distingue expresamente los
atributos de cuenta de las contraseñas: no exporta ni demuestra restauración de
hashes de contraseña. Actions conserva `emulator-media-drill.json` con este alcance.

## Antes de una migración o una recuperación real

### Aprobación de costos pendiente

El propietario autorizó consultar y presentar el costo antes de activar servicios,
no activarlos todavía. No se configura retención de pago ni se restaura producción.
El inventario incluye la región cuando la API la permite. Antes de pedir aprobación:

- Medir tamaño actual, crecimiento y volumen de adjuntos sin exportar datos personales.
- Cotizar en https://cloud.google.com/firestore/pricing según región y moneda:
  almacenamiento retenido, frecuencia y retención de copias, costo de restauración
  y almacenamiento temporal de la base aislada. Incluir identidades y archivos
  por separado: una copia de documentos no protege ambos.
- Presentar el total mensual estimado, el costo excepcional de un ensayo y límites
  de retención. No confundir alertas de presupuesto con un límite duro de gasto.
- Registrar aprobación explícita del costo y del destino aislado antes de activar.

Sin tamaño, región y tarifa vigente comprobados no se publica una cifra inventada.
`activationStatus=PENDING_COST_REVIEW` no equivale a protección ni ensayo completado.

### Referencia de cálculo consultada el 23 de octubre de 2026

La página pública de precios muestra para Iowa (`us-central1`), tarifa estándar
en USD, almacenamiento de copias a 0.000041096 por GiB/hora y restauración a 0.20
por GiB. Son referencias de esa región, no una cotización de la cuenta de Coach.
Con 730 horas, una copia de 1 GiB retenida un mes equivale a aproximadamente 0.03 USD.
Siete copias diarias de 1 GiB mantenidas de manera estable rondarían 0.21 USD/mes;
un ensayo de restauración de 1 GiB añadiría 0.20 USD por la operación, más la base
temporal, posibles lecturas/transferencias, adjuntos, impuestos y cualquier otra
partida aplicable. El costo real depende de región, tamaño y retención confirmados.
Fuente: https://cloud.google.com/firestore/pricing (tabla por ubicación y apartado de copias).
No se autorizó ni activó facturación con este cálculo de referencia.

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
