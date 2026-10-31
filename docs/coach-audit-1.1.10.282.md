# Coach 1.1.10.282 — referencias temporales y gestión compacta

Versión anterior: 1.1.10.281 (997). Versión nueva: 1.1.10.282 (998).

## Problemas confirmados y correcciones

- Se retiraron de la clasificación los párrafos de diagnóstico. Solo queda un estado breve de actualización y su hora real.
- La variación se calculaba contra la consulta anterior, por eso una respuesta idéntica la devolvía a +0.00%. Ahora compara contra una referencia histórica: cero es neutral, sin flecha positiva; una referencia ausente se muestra como guion. Los puntos porcentuales se identifican como `pp`.
- La gráfica anterior era demasiado pequeña y dependía de acumular horas en cada instalación. Se aumentó su área y se incorporó el archivo histórico fechado de WildRiftFire. Las observaciones propias tienen prioridad; cuando se usa un informe diario se muestra `Referencias diarias publicadas`, su fecha y `ref. diaria` en la variación. Una referencia diaria no se presenta como medición horaria exacta, no se interpola y no se mezclan historiales de fuentes o parches distintos. Si falta una muestra, permanece ausente.
- Se mantienen consultas de red cada minuto mientras la aplicación o el hub están activos. La tarea de segundo plano pasa de una hora al mínimo periódico de quince minutos de Android; el sistema puede aplazarla. Consultar continuamente no obliga al editor externo a cambiar sus porcentajes cada minuto.
- Cinco fuentes supervisadas: las tres existentes más WildLegends y WildRifter. Se comprueba el parche de cada respuesta antes de incorporarla al consenso. Una fuente incompatible se conserva visible como incidencia y no aporta datos.
- Se lee también el informe estadístico de WildRiftFire, con el mismo tramo Maestro+ para los datos actuales y su archivo histórico. Otras fuentes solo completan campeones ausentes; no se promedian mediciones de tramos distintos. Se conserva la procedencia de cada observación.
- El monitoreo presenta cinco filas con estado, cobertura y hora, y avisos persistentes por consulta detenida, datos parciales, fallo de estadísticas o parche incompatible. Cada fila abre los detalles técnicos actualizados. Cada respuesta actualiza su estado sin esperar a la fuente más lenta. El seguimiento de tasas sin cambios es independiente del cambio de categorías, para que una actualización de tiers no oculte tasas estancadas.
- Los accesos superiores se distribuyen en varias filas sin desplazamiento horizontal. Se agrega el acceso a Usuarios; la búsqueda, filtros y gestión se abren en su propia pantalla, con lo que no se carga el listado debajo del monitoreo.

## Auditoría de fuentes del 15/10/2026

Las cuentas indican datos reconocidos del catálogo local, no enlaces de navegación.

| Fuente | Parche recibido | Categorías | Tasas en la página principal | Resultado |
|---|---|---:|---:|---|
| BestBuildWR | 7.3a | 142 | 0 | Excluida del consenso 7.3b y señalada en monitoreo |
| WildRiftFire | 7.3b | 142 | 0 | Informe separado con 137 tasas e historiales Maestro+ |
| WildRiftCore | 7.3b | 142 | 137 | Aceptada |
| WildLegends | 7.3b | 142 | 0 | Aceptada; catálogo completo de 142 |
| WildRifter | 7.3b | 141 | 141 | Catálogo de 142; Master Yi sin datos publicados |

Fuentes nuevas verificadas:
- https://wildlegends.net/en/tier-list
- https://wildrifter.com/en?patch=7.3b

Historial publicado: https://www.wildriftfire.com/stats. La cobertura combinada verificada es de 142/142 tasas completas, sin inventar valores. Se normaliza el identificador publicado `nunu-amp-willump` para no perder las estadísticas e historial de Nunu y Willump. La pantalla no promete cinco fuentes sanas cuando alguna está incompleta o desactualizada.

## Archivos modificados

- Versión Gradle, `WildRiftApp`, modelo `Champion`, repositorio y traducciones UI de portugués.
- Sincronización: `GlobalTierSourceClient`, `BestBuildWrScraper`, `RegionalTierParser`, `PublishedStatsParser`, `MetaFreshness`.
- Interfaz: `MetaSyncSummary`, `MetaMonitoringCard`, `RateChangeBadge`, `SparklineTrendGraph`, `TierListTab`, `ChampionDetailSheet`, `AdminDashboardDialog`, `UserManagementPanel`.
- Pruebas: `PublishedMetaReportTest`, `MetaControlsLayoutTest`, `GlobalTierSourceClientTest`, `MetaRegionRegressionTest`.

## Verificación

- Dieciséis pruebas JVM de parser, cliente HTTP, validación del parche, historial y comparación de versiones aprobadas localmente.
- Parseo con las respuestas reales de los cinco editores; se detectó y descartó el parche anterior de BestBuildWR.
- Regresiones Android para la referencia de 24 horas, rechazo de un parche anterior, tres valores visibles, cero neutral y accesos en varias filas en una pantalla estrecha.
- Comprobaciones obligatorias de Android, idioma portugués, R8, firma persistente y APK instalado en español y portugués antes de fusionar y publicar.
