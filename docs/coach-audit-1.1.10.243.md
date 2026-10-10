# Coach 1.1.10.243 — código 959

## Cambios

- El décimo retrato conserva la última selección válida durante lecturas vacías o ambiguas. Una detección distinta reemplaza solamente el décimo campeón seguido, respetando los bloqueos manuales y los nueve anteriores.
- El seguimiento visual continúa después de completar 10/10; la sincronización global sigue ejecutándose periódicamente. Cuando desaparecen todos los slots, se conserva el resultado y se desactiva el escáner automático tras dos fotogramas ausentes consecutivos.
- El detalle de cada runa situacional muestra sus efectos del catálogo y su consejo individual de la build por separado. Se conservan las traducciones propias en español y portugués.
- Réplica deja de ser un alias de Garras del Inmortal o Soberano Gélido. Se eliminan otras equivalencias entre runas diferentes y las coincidencias parciales que podían abrir detalles ajenos; iconos y detalles usan el mismo resolver.

## Archivos

- `FloatingOverlayContent.kt`, `OverlayState.kt`, `ConfirmedLastPickHud.kt`.
- `DraftVisionScanner.kt`, `DraftSyncCadence.kt`, `DraftSlotLifecycle.kt`, `TenthPickHudPolicy.kt`, `LiteRTVisionClassifier.kt`.
- `WildRiftSpellsAndRunes.kt`, `BuildElementAdvice.kt`, `ChampionDetailSheet.kt`.
- Pruebas de ciclo del draft, reemplazo del décimo retrato e identidad de runas.
- `app/build.gradle.kts`.

## Verificación

- Comprobación local de catálogos generados y diferencias sin errores de formato.
- Pruebas, compilación ofuscada y validación instalada en español/portugués pendientes de Actions; no hay SDK Android ni configuración privada local.

## Resumen para testers

Coach 1.1.10.243: comprobar que el décimo campeón permanezca seleccionado durante lecturas sin resultado y cambie solamente cuando aparezca otro retrato válido. Al desaparecer los slots, el escáner automático debe apagarse y conservar la última selección. Abrir dos runas situacionales distintas y verificar que sus efectos y consejos correspondan a cada runa, en español y portugués.
