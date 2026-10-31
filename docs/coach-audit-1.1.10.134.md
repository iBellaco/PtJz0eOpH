# Auditoría Coach 1.1.10.134 (850)

## Cambios visibles

- El consejo de la build se muestra directamente bajo el título «Consejo del coach»; ya no requiere pulsar «Ver consejo de la build».
- Los 142 campeones y sus 300 perfiles por rol disponen de 12 ventajas, 12 debilidades y 12 sinergias. Premium muestra cuatro filas de tres en cada categoría. Se mantienen los límites de tres para invitados y seis para cuentas gratuitas.
- Las listas excluyen al propio campeón y los duplicados. Ventajas y debilidades no se solapan. Los rivales pueden jugar el rol seleccionado.
- El creador se abre en una ventana completa, con fondo opaco y márgenes del sistema. Cubre la navegación inferior de Coach y evita que se pueda pulsar mientras está abierto; al cerrarlo vuelve la pantalla anterior.
- El título y los nuevos estados de actualización tienen traducción portuguesa.

## Origen de las recomendaciones

Las referencias editoriales existentes conservan su prioridad. Cuando faltan nombres para completar doce, Coach ordena candidatos por interacciones tácticas del kit y del rol: acceso a campeones de distancia, protección contra entradas, control, hostigamiento, recuperación, daño contra primera línea, daño en área y complementariedad de aliados. Los pares añadidos son recomendaciones del coach; no se presentan como estadísticas obtenidas de los sitios de referencia. No se añaden porcentajes de victoria inventados. La cobertura completa no sustituye una revisión de equilibrio por parche.

## Scraping de las tres fuentes

| Fuente | Problema corregido | Campeones validados en consulta real |
| --- | --- | ---: |
| BestBuildWR | Los tiers están en claves de un diccionario dentro de `tierData`, no como un atributo de cada campeón | 141 |
| WildRiftFire | Lectura de contenedores DOM delimitados; se evita arrastrar el último tier al catálogo del pie y descartar nombres que contienen «es» | 141 |
| WildRiftCore | Lectura de filas con `data-tier` y del panel combinado, evitando duplicados y enlaces de builds | 142 |

La combinación de las tres consultas cubrió los 142 campeones del catálogo de Coach. Las consultas reales se realizaron con el mismo cliente HTTP y parsers del proyecto, usando únicamente la configuración de proxy necesaria para la red del entorno de revisión. Se mantuvo la verificación TLS.

- Extracción separada por fuente, con validación de IDs del catálogo y mínimo de cobertura antes de aceptar una respuesta.
- Consultas independientes en paralelo, tiempos máximos y un reintento para fallos temporales.
- Un HTTP correcto con una página vacía o de error no cuenta como una fuente saludable.
- Eliminados estados iniciales ficticios de salud y latencia. Se informa de cuántas fuentes aportaron categorías válidas.
- Los fallos conservan los últimos datos. Las actualizaciones parciales conservan los campeones ausentes del nuevo resultado.
- Un único arranque de la consulta evita duplicar las peticiones. La entrada de sincronización que antes estaba vacía ahora delega al proceso real.

## Validación

- 21 pruebas JVM correctas: reglas de builds/permisos, cobertura de recomendaciones, parsers, cliente HTTP, conservación de resultados y portugués.
- Auditoría integrada de 300 builds y 300 perfiles por rol: las 900 listas tienen exactamente 12 nombres, sin autorreferencias, duplicados ni solapamiento entre ventajas y debilidades.
- Tres fuentes comprobadas con respuestas HTTP reales; combinación de 142 campeones.
- Sintaxis Kotlin revisada y diferencias sin errores de formato.
- Auditoría portuguesa ampliada: 1.154 frases Kotlin y 4.255 frases totales. Los 29 resultados sin cambio siguen correspondiendo a expresiones internas y nombres compartidos entre idiomas.

## Pendiente en Android

El entorno no dispone del SDK Android; no se compiló ni ejecutó la APK. Falta comprobar visualmente el consejo en cada opción, las cuatro filas de cada categoría Premium, los márgenes del creador en pantallas pequeñas y el regreso a la navegación al cerrarlo. Las comprobaciones HTTP actuales no garantizan la disponibilidad futura de servicios externos.
