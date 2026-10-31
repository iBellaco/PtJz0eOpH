# Coach 1.1.10.281 — consulta continua, monitoreo y versión obligatoria

Versión anterior: 1.1.10.280 (996). Versión nueva: 1.1.10.281 (997).

## Causas comprobadas

- La consulta al abrir y el trabajo periódico de una hora no constituían una actualización continua. El bucle del monitoreo invocaba una función que omitía consultas durante quince minutos.
- La página de categorías solo exponía tasas en su podio. La tabla completa de clasificaciones expone tasas para prácticamente todo el catálogo.
- Los formateadores y el adaptador de línea inventaban porcentajes y variaciones cuando faltaba información. Las gráficas interpolaban supuestas tasas de 12 y 24 horas a partir de un único delta.
- La comprobación de versiones estaba desactivada. El comparador anterior perdía la parte final de versiones con sufijo de build y el diálogo ofrecía descarga y posposición.

## Resultado

- Un único ciclo consulta cada 60 segundos mientras la aplicación o el hub están abiertos. Al cerrar ambos, se detiene; el trabajo periódico existente cubre segundo plano según las restricciones de Android. Las peticiones exigen consulta de red y conservan la última observación cuando fallan.
- Se consulta la tabla completa de tasas, con validación de rango 0–100 y aceptación de cero. Las categorías siguen dependiendo de las fuentes publicadas; un S+ puede permanecer igual cuando estas no cambian su clasificación.
- Se eliminan tasas de relleno, penalizaciones ficticias para roles secundarios y tendencias inventadas. La pantalla muestra valores publicados, la hora de consulta y guiones para datos ausentes. Se conserva un historial local acotado de observaciones reales; los puntos de 12 y 24 horas se habilitan únicamente cuando hay muestras cercanas verificadas.
- Monitoreo por fuente: último intento, último éxito, último cambio, latencia, número de categorías y tasas, consultas sin cambios y cobertura incompleta. Alerta por consulta vencida tras tres minutos y aviso por valores iguales durante 24 horas, diferenciando una fuente sin cambios de una consulta detenida.
- La Tier List global muestra únicamente S+, S y A (A+ se agrupa en A), incluyendo la ordenación por tasas.
- El hub y las fichas de enfrentamiento e historial resuelven las tasas contra el campeón vigente; el hub muestra dos decimales en lugar de truncar valores a enteros. Los resultados guardados se conservan.
- El historial personal sigue recalculándose con los resultados guardados y actualiza también la ficha abierta y los cambios de catálogo.
- La versión se comprueba al abrir y cada cinco minutos de uso. Una release estable con APK y build superior activa un aviso obligatorio antes de mostrar la aplicación y detiene el hub. La obligación conocida se conserva sin conexión. El aviso solo informa y permite volver a comprobar: no abre descargas, no instala y no se pospone.

## Archivos principales

- `data/sync/{GlobalMetaSyncService,BestBuildWrScraper,GlobalTierSourceClient,RegionalTierParser,MetaFreshness}.kt`
- `data/WildRiftRepository.kt`, `model/Champion.kt`
- `util/{AppUpdateManager,PublishedAppVersion,ChampionRoleAdapter,RegionalStatsFormat}.kt`
- `MainActivity.kt`, `service/FloatingAssistantService.kt`
- `ui/components/{MetaSyncSummary,UserManagementPanel,AppUpdateDialog,SparklineTrendGraph}.kt`
- `ui/screens/{TierListTab,ChampionsCatalogTab,ChampionDetailSheet,PersonalTierListView}.kt`
- Traducciones de interfaz en portugués, versión Gradle y regresiones de fuentes, historial y bloqueo de versión.

## Verificación

- Auditoría de fuentes reales del 15/10/2026: 142 categorías y 137 fichas con tasas completas frente a 14 fichas del podio. Akshan, Bardo, Corki, Nilah y Zoe no tienen fila de tasas en la tabla observada; se informa la cobertura faltante y no se inventan sus valores.
- Pruebas JVM de parser, fusión, historial real, datos vencidos y comparación de tags: 12 casos correctos.
- Regresión de Compose para comprobar que una ficha de enfrentamiento abierta recibe nuevas tasas aunque el parámetro original del campeón siga siendo la misma copia.
- Auditoría portuguesa de código fuente: sin residuos en español.
- Se corrigió una importación faltante de color que la primera compilación Android detectó antes de publicar.
- La auditoría instalada portuguesa detectó nombres traducibles sin localizar en el aviso de cobertura; se aplica el traductor existente a cada campeón antes de repetir la validación.
- Validación Android, R8, firma persistente y APK instalado en español y portugués mediante las comprobaciones obligatorias de Actions antes de fusionar.
