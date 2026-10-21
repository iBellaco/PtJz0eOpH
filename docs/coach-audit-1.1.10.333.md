# Auditoría Coach 1.1.10.333 — Wild Rift 7.3c

## Catálogo y parche

- Se consultaron las notas oficiales de Riot para 7.3c: https://wildrift.leagueoflegends.com/es-mx/news/game-updates/wild-rift-patch-notes-7-3c/
- 7.3c solo publica cambios de Aumentos de ARAM AAA y ajustes de campeones. No publica cambios de objetos, runas ni hechizos de invocador.
- Por esa razón el catálogo mantiene intactos los nombres, etiquetas, estadísticas, descripciones e iconos existentes. Solo se actualizó el contexto de parche visible y la validación de fuentes a 7.3c.
- No se añadieron porcentajes ni valores inferidos desde terceros.

## Scraping y monitoreo

- WildRiftGuides fue eliminado de las fuentes activas, del consenso estadístico y de las fuentes mostradas en el repositorio.
- El monitoreo estadístico queda limitado a WildRiftFire y WildRiftCore, ambos rechazando respuestas que no identifiquen el parche 7.3c.
- La fusión conserva el último snapshot válido cuando una fuente falla; una respuesta HTTP correcta sin parche compatible no se considera válida.
- El tiempo de “sin cambios” se calcula a partir de la huella persistida de categorías y estadísticas por fuente, no de la hora de la última consulta.

## Tier list

- La vista de Tier List deja de mostrar la métrica de pick rate/“% PP”. El dato puede seguir almacenándose para auditoría interna, pero no se presenta como una categoría o recomendación visual.

## Estado de publicación

- La ejecución más reciente de `main` sobre el commit anterior terminó correctamente; las X visibles pertenecen a ejecuciones históricas canceladas o fallidas.
- Esta versión incrementa `versionCode` a 1049 y `versionName` a 1.1.10.333. La publicación real queda condicionada a que Actions complete los controles de firma, catálogo, scanner, seguridad y release.
