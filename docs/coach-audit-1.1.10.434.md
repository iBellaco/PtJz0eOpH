# Coach 1.1.10.434 — build 1150

## Cambios

- Control Activar: ya existía en AdminOverlayAccessDialog y MainDraftingScreen. Se elimina el falso éxito de lectura: un fallo no se convierte en configuración habilitada ni permite guardar valores por defecto sobre una desactivación remota. El visor falla cerrado si no puede cargar su configuración.
- Patrocinios: la revisión y los contadores incluyen economy_requests de tipo SPONSOR, incluso en REVIEW, además de pending_sponsor_ads. Una solicitud cargada no se duplica y una resuelta no reaparece por una cola atrasada. Las pestañas se desplazan horizontalmente en pantallas pequeñas.
- Resolver un envío en cola verifica la autoridad y reutiliza la operación original. Cancelar uno sin cobro no genera débito ni reembolso ficticio; el control transaccional también bloquea al worker concurrente. Rechazar uno cobrado usa la devolución atómica existente, comprobando el cargo e impidiendo una segunda devolución.
- Se eliminan OrangeEssenceRedemptionEntry y OrangeEssenceRedemptionDialog, sus accesos y contadores. Se bloquea REDEEM en servidor y reglas para instalaciones antiguas. Se conservan registros y liquidación de obligaciones anteriores; no se borran saldos ni comprobantes.
- Acciones del perfil agrupadas con separación uniforme de 12 dp, incluso cuando cambia el conjunto de botones visibles.
- Direcciones de categoría persistentes por GamePatch.VERSION, sin caducidad temporal. Una instantánea compartida de categorías y direcciones se publica transaccionalmente desde una sesión con claim válido y se observa desde los demás dispositivos. Sin conexión se conserva el último estado local; al cambiar el parche se empieza otro documento y otra caché. La publicación compartida requiere que una sesión autorizada sincronice las fuentes; no se atribuyen cambios inventados en la primera lectura.
- Solicitud coach-validacion → main obligatoria al promover cambios, con fusión manual. Build and Release APK se ejecuta por push en validación/main, evitando duplicarlo al abrir la solicitud.

## Servicios externos comprobados

- Firebase Authentication, Firestore y Functions: identidad, sincronización y operaciones privadas; coachEconomy cuenta con cola de respaldo.
- GitHub REST: consulta de la última actualización en AppUpdateManager.
- QRServer: generación remota de QR en DonationDialog.
- WildRiftFire y WildRiftCore: descarga y lectura de páginas públicas para categorías/estadísticas, no una API oficial de partidas de Riot.
- Data Dragon/CommunityDragon y enlaces de medios: recursos gráficos y reproducción.
- No se encontraron llamadas a Gemini. Se eliminó la declaración MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API de metadata.json por no corresponder a una integración implementada.

## Verificación

- 46 pruebas de políticas Node y 30 casos de transacciones en emuladores aislados superados, además de las pruebas de permisos; incluyen aprobación desde cola, reembolso repetido, cancelación sin débito y retiro de nuevos canjes.
- Recursos españoles y portugueses, auditoría de textos y pruebas de planificación/reutilización de validación.
- Nuevas pruebas PatchTierMovementTest en el flujo fix existente; no se inicia otro flujo paralelo.
- La compilación Android local no pudo descargar Gradle (Connection refused). La compilación y pruebas Compose quedan pendientes de fix y validación; no se afirma prueba física.
- Los cambios de servidor y reglas requieren el despliegue asociado a la integración manual en main. No se han modificado saldos ni documentos de producción durante esta intervención.
