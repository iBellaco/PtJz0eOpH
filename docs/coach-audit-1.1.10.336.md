# Coach 1.1.10.336 — Build 1052

## Cambios

- Limpieza de cuentas: se excluyen accesos recientes, identidades externas,
  cuentas deshabilitadas, permisos especiales, roles, beneficios, saldos e
  historial privado. Se revalida el estado antes de eliminar; los errores se
  informan y una cola privada permite recuperar perfiles pendientes.
- Scanner físico: herramienta de captura de metadatos y evaluación de evidencias
  con p95, precisión y comprobaciones de orientación y persistencia. Los tres
  perfiles físicos siguen pendientes de mediciones, sin resultados inventados.
- Auditoría instalada: reintento limitado a errores transitorios del árbol UI;
  los errores funcionales y crashes fallan sin reintento. Ambas evidencias quedan
  retenidas en los artefactos de Actions.
- Parche: `GamePatch.VERSION` centraliza la versión activa de la app y fuentes;
  una caché antigua no puede sustituirla. Se validan declaraciones actuales y
  traducciones referenciadas, conservando las referencias históricas de fuentes.
- Scraper: umbral del 70 % redondeado hacia arriba (100/142), mensaje numérico
  claro y cobertura sin duplicados. Pruebas de límite y campeones desconocidos.
- Recuperación: inventario de copias de solo lectura, resultado explícito ante
  falta de permisos y ensayo en disco con restauración de registros anidados y
  conservación de tipos dentro de emuladores aislados.

## Archivos

`app/build.gradle.kts`, `GamePatch.kt`, `WildRiftRepository.kt`,
`WildRiftLocalCache.kt`, `BestBuildWrScraper.kt`, `GlobalTierSourceClient.kt`,
`GlobalTierSourceClientTest.kt`, `RuntimeBehaviorTest.kt`, scripts y pruebas de
parche/reintento/escáner, servicio y pruebas de limpieza/inventario/recuperación,
`tests/firestore/{run-tests,rules.test}.mjs`, workflows de APK y cuentas,
`test_scraper.py`, `physical-device-smoke-checklist.md`, `recovery-runbook.md`.

## Verificación y límites

Se ejecutan pruebas Python y Node; Actions debe completar reglas, servicios,
scanner, suite Android, lint, firma persistente, ofuscación y APK instalado en
español y portugués antes de publicar. El resultado de Actions y el inventario
productivo se registrarán al finalizar la entrega.

Las pruebas sintéticas no sustituyen dispositivos físicos ni una restauración de
una copia productiva. No se crean servicios de pago ni se restaura sobre datos
reales. La limpieza cruza dos APIs y no puede hacerlas atómicas; las lecturas
finales y la cola privada cubren la comprobación y recuperación entre operaciones.
