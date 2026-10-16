# Coach 1.1.10.301 — build 1017

## Correcciones

- Alineación del draft por aros visibles: busca las columnas dentro del fotograma y exige coincidencia entre varios slots. Descartamos los aros del visor sobre fondo vacío mediante contraste del interior. Se excluye el HUD opaco; no se infiere un avatar oculto debajo de él. El visor consume la misma configuración efectiva que el lector. Los ajustes manuales conservan prioridad.
- Premium: se eliminan dos límites internos de 12. La auditoría comprueba 24 ventajas, 24 debilidades y 24 sinergias únicas para los 142 campeones y 300 perfiles de rol; invitado 3, registrado 6, Premium 24. Las relaciones publicadas tienen prioridad; las ampliaciones tácticas son inferencias del coach, no tasas de victorias medidas.
- Fuentes de relaciones separadas del monitoreo de estadísticas. Las cinco direcciones solicitadas se conservan como referencias de enfrentamientos. El monitoreo mantiene los publicadores de estadísticas usados por la aplicación, WildRiftFire y WildRiftCore.
- Consulta actual: WildRiftCounter permitió actualizar 138 páginas. BestBuildWR, WildRiftFire, WildRiftCore y TheWildPick devolvieron HTTP 403. Se conservan sus referencias históricas disponibles sin atribuirles una revisión nueva ni inventar contenido. Evidencia: `matchup-source-refresh-1.1.10.301.json`.
- Eliminadas las gráficas por campeón en clasificación y catálogo y el encabezado de tendencias que quedaba sin gráfica. La ficha ya no dibujaba gráfica y conserva las tasas numéricas.
- Corregida comparación de categorías S y A; los iconos conservan el último movimiento real durante consultas sin cambios.
- Exportación 9:16 a 2160×3840, solo S+, S y A (A+ se integra en A igual que en pantalla), avatares locales y estadísticas en una línea. Filas ajustadas al número de campeones. Pie reservado para el logo de Coach centrado y más grande y el logo oficial de Wild Rift abajo a la derecha. Fecha arriba a la derecha. Fondo del mapa de la Grieta.

## Recursos

- Fondo del mapa: https://ddragon.leagueoflegends.com/cdn/15.1.1/img/map/map11.png
- Logo oficial Wild Rift: https://cmsassets.rgpub.io/sanity/images/dsfx7636/news/74f09240c5a0dfce67f3cf2e9611ee1a9cfb0b1e-736x500.png
- Se incluyen localmente; la exportación no descarga imágenes.

## Archivos y comprobaciones

- Captura: `DraftRingAlignment.kt`, `DraftVisionScanner.kt`, `ScannerDebugOverlay.kt`, `DraftRingAlignmentTest.kt` y captura de regresión S26.
- Relaciones: `ChampionRoleMatchupAdvisor.kt`, `CoachMatchupRanking.kt`, `WebMatchupReferences.kt`, `CoachMatchupCoverageTest.kt`, `RuntimeBehaviorTest.kt`.
- Presentación: `TierListExport.kt`, `TierListExportRenderedTest.kt`, `TierListTab.kt`, `ChampionsCatalogTab.kt`, `RuntimeVisibilityTest.kt`.
- Fuentes y categorías: `WildRiftRepository.kt`, `BestBuildWrScraper.kt`.
- Versión: `app/build.gradle.kts`.
- Verificación local de la captura: columna rival visible ≈1170/1280; posición previa 1227/1280. `git diff --check`.
- Se requieren las comprobaciones completas de Actions, el APK firmado y ofuscado instalado y las pantallas en portugués antes de integrar. La prueba en un S26 físico sigue pendiente del tester; la captura se conserva para reproducir el desplazamiento.
