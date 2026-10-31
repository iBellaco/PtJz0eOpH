# Auditoría Coach 1.1.10.268 (build 984)

## Alcance

- Se revisó el flujo de recomendaciones con la opción **Blind Pick** activa y sin retratos detectados.
- Se añadió una única build contextual para el campeón de la línea o, mientras no exista uno, para la recomendación principal.
- Se mantuvieron las relaciones de ventaja, debilidad y sinergia que ya calcula `DraftScoringPolicy`.

## Reglas de la build contextual

- La build siempre intenta mostrar tres objetos principales y resuelve cada nombre contra el catálogo local de objetos.
- Los objetos situacionales cambian según la composición rival: curación, escudos, primera línea resistente y asesinos.
- Las runas se toman del campeón y se completan con opciones del catálogo apropiadas para la amenaza detectada.
- Jungla conserva Destello+Aplastar; Soporte conserva Destello y un segundo hechizo contextual, además de Hoz espectral o Escudo reliquia según el perfil del campeón.
- No se presentan entradas que no existan en los catálogos locales.

## Verificación

- Se añadieron pruebas para asegurar que Blind Pick no quede vacío, que la build tenga tres objetos catalogados y que el soporte conserve su objeto inicial.
- `git diff --check` pasa.
- La ejecución local de Gradle no pudo comenzar porque el wrapper intenta descargar Gradle 9.3.1 y el entorno de ejecución no tiene acceso de red. La validación completa queda delegada al flujo de GitHub Actions.

