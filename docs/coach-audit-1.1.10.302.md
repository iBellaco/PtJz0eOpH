# Coach 1.1.10.302 — build 1018

## Cambios

- El HUD detiene el escáner al reconocer el décimo campeón con diez selecciones únicas; no espera a que desaparezca la pantalla de selección. Abre el mismo `FloatingSaveMatchDialog` del botón Guardar. Las capturas automáticas se desactivan con el escáner. Los resultados tardíos no alteran el draft mientras el diálogo está abierto y cerrarlo no lo vuelve a abrir. Vaciar también cierra el diálogo.
- Las fuentes de clasificación son exactamente WildRiftCore `https://wildriftcore.com/en/tierlist/`, WildRiftGuides `https://www.wildriftguides.com/tier-list` y WildRiftFire `https://www.wildriftfire.com/tier-list`. Las tres respondieron HTTP 200 e identifican 7.3b en las páginas consultadas el 16/10/2026. Se agrega un lector de las filas reales de Guides, limitado a cada sección de rol; no extrae campeones de navegación ni inventa porcentajes cuando solo hay categorías.
- Cinco categorías visibles: S+, S, A, B y C. Las referencias A+ se integran en A y D en C; los indicadores distinguen también movimientos entre A, B y C. La lista no oculta los campeones B/C.
- Se retira el botón de descarga. Pick usa cian y Ban rojo tanto en las tarjetas por categoría como en las listas ordenadas. Se eliminan los textos «ref. diaria» y «Referencias diarias publicadas»; se mantienen los valores y los indicadores numéricos de cambio.

## Archivos

- `OverlayState.kt`, `ConfirmedLastPickHud.kt`, `FloatingOverlayContent.kt`.
- `BestBuildWrScraper.kt`, `RegionalTierParser.kt`, `GlobalTierConsensus.kt`, `WildRiftRepository.kt`.
- `TierListTab.kt`, `ChampionRateLine.kt`, `RateChangeBadge.kt`, `ChampionDetailSheet.kt`, `SparklineTrendGraph.kt`.
- `DraftAutoStopTest.kt`, `GlobalTierScrapingTest.kt`, `MatchupReferenceSourcesTest.kt`, `MetaControlsLayoutTest.kt`, `TierMovementRegressionTest.kt`, `TierCategoriesRenderedTest.kt` y capturas HTML públicas `tier-sources-302/`.
- `app/build.gradle.kts`.

## Verificaciones

- Nueve pruebas JVM del lector y consenso ejecutadas contra el código Kotlin real: correctas, incluidas las páginas descargadas de las tres fuentes y la verificación de su parche.
- Catálogos, iconos y 300 builds: comprobaciones locales correctas.
- Auditoría portuguesa de código: 2622 ocurrencias, cero textos españoles detectados.
- Regresiones Android: nueve selecciones y retrato sin confirmar no detienen el escáner; décima confirmada abre Guardar una vez; no se permiten duplicados; los diez campeones quedan congelados mientras se ofrece guardar. Cinco secciones alcanzables, sin botón de descarga; indicadores A/B/C.
- Requeridas antes de publicar: suite Android completa, pantallas portuguesas, APK firmado/ofuscado instalado en ambos idiomas y flujo main verde.
