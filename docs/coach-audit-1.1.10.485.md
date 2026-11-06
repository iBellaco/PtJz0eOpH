# Coach 1.1.10.485 — Modificación del Hub, la Tier List y los planes Premium

## Actualización realizada

- El Hub del overlay ya no muestra los accesos manuales de visor y depuración.
  Se conserva únicamente minimizar, mientras el escáner sigue usando la
  geometría multidispositivo.
- La etiqueta «Auto-Scan» tiene una pulsación visual mientras el escáner está
  activo y conserva un objetivo de prueba accesible.
- Los cambios de categoría de la Tier List conservan la magnitud real. La
  insignia muestra el icono y `x1`, `x2` o `x3`; la fuente de sincronización ya
  no reduce todos los movimientos a una sola categoría.
- Premium ofrece 1 mes, 6 meses, 1 año, 1 año y 6 meses y 2 años. Se fijaron
  los precios en EA solicitados y los precios equivalentes en EN (9, 50, 95,
  145 y 190), con el ahorro visible y animado en cada plan que lo aplica.
- El servicio autoritativo y la aplicación comparten los identificadores,
  duraciones, precios y etiquetas de los cinco planes.
- Se añadió a las instrucciones del proyecto la obligación de titular cada
  entrega según el cambio actual, sin reutilizar títulos de tareas anteriores.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/main/java/com/example/ui/components/TierMovementBadge.kt`
- `app/src/main/java/com/example/data/sync/PatchTierMovement.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/data/EssenceEconomyPolicy.kt`
- `app/src/main/java/com/example/ui/components/SubscriptionPlansBottomSheet.kt`
- `server/economy/policy.mjs`
- `server/economy/service.mjs`
- `AGENTS.md`

## Verificación

- `server/economy`: 52 pruebas unitarias correctas, incluidos los cinco
  planes en ambas monedas.
- `bash tools/audit-portuguese.sh`: 2.528 literales visibles revisados, sin
  hallazgos en español; se añadieron las etiquetas y ahorros de los planes al
  catálogo portugués.
- `python3 tools/update-champion-builds.py --check`: datos generados vigentes.
- `node --check` para las funciones de economía y `git diff --check` correctos.
- `./gradlew :app:testDebugUnitTest` no pudo iniciar porque este entorno no
  tiene el SDK de Android (`SDK location not found`). La compilación release,
  la firma persistente, R8 y la prueba instalada quedan para la validación
  remota de producción.

## Validación pendiente

- Probar visualmente el Hub sin controles manuales, la animación de Auto-Scan,
  los multiplicadores de movimiento y la selección de cada plan Premium en
  español y portugués en un APK de producción.
