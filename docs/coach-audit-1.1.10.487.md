# Coach 1.1.10.487 — Planes Premium, operaciones administrativas inmediatas y favoritos del draft

## Cambios implementados

- Se actualizaron los planes Premium en azul y naranja:
  - 1 mes: 100 EA / 9 EN.
  - 6 meses: 550 EA / 50 EN.
  - 1 año: 1100 EA / 95 EN.
  - 1 año y 6 meses: 1580 EA / 138 EN.
  - 2 años: 2050 EA / 175 EN.
- Los ahorros se recalculan desde la tarifa mensual y los botones de Esencia
  Naranja siguen ocultos cuando el saldo EN es cero.
- Las acciones administrativas de aprobación/rechazo de anuncios y ajuste de
  esencias priorizan la confirmación directa. La cola queda únicamente como
  recuperación si el transporte directo no está disponible; la verificación de
  la sesión y de la autorización firmada se conserva en el servicio.
- Se mantiene una instancia activa del servicio económico para reducir la
  latencia de arranque en decisiones y ajustes administrativos.
- Se añadió un filtro opcional de recomendaciones del draft. Cuando hay al
  menos cinco favoritos compatibles con la línea activa, las recomendaciones se
  limitan a ese conjunto, incluido el primer pick. El filtro se puede activar o
  desactivar y se conserva entre sesiones.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/data/EssenceEconomyPolicy.kt`
- `app/src/main/java/com/example/data/EconomyServiceClient.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/ui/screens/MetaAndDraftScreen.kt`
- `app/src/main/java/com/example/ui/screens/DraftAnalysisTab.kt`
- `server/economy/policy.mjs`
- `server/economy/index.mjs`
- `app/src/main/assets/translations_ui_pt.json`

## Verificaciones

- `server/economy`: 52 pruebas Node superadas.
- `node --check` superado para `policy.mjs`, `service.mjs` e `index.mjs`.
- `git diff --check` superado.
- La suite Android no pudo ejecutarse en este entorno porque no hay SDK
  instalado ni una ruta `ANDROID_HOME`/`local.properties` disponible.
