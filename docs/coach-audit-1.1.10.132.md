# Coach 1.1.10.132 — auditoría del 1 de octubre de 2026

La revisión corrige la cobertura del portugués y simplifica la consulta de builds. La auditoría es estática y usa pruebas JVM; la compilación del APK y el recorrido visual en Android siguen pendientes porque este entorno no dispone del SDK Android.

## Cambios visibles

- Información legal: una sola acción inferior para cambiar idioma y otra para rechazar/salir. Se retiraron los dos iconos superiores. Atrás usa la acción de cerrar, sin abrir el selector de idiomas.
- Builds: consejo general al pulsar su botón; consejos específicos al pulsar objetos, botas, runas y hechizos. Las botas conservan la selección reversible. El consejo general también admite la vista flotante.
- Runas: iconos conectados por una flecha. Cuando se conoce el grupo de sustitución, se muestra la runa concreta; las opciones sin grupo identificado muestran las tres secundarias de la misma rama. La cuarta secundaria puede recibir otra rama. No se muestra el texto “Alternativa”.
- Ventaja, debilidad y sinergia: máximos de tres para invitados, seis para cuentas con sesión y doce para Premium. Se ven dos filas de tres y el resto se consulta desplazando cada sección.
- Tier list: Global es la única opción. Se retiraron el selector, la consulta china, su caché visible y sus textos. Una preferencia antigua pasa a Global y se guarda migrada.

## Portugués

Se analizaron 1.574 frases y plantillas de interfaz y 4.736 frases únicas al incluir los campos de los catálogos. Se corrigieron etiquetas, mensajes, frases con cantidades y 1.668 consejos compuestos que quedaban en español al incorporar rol y campeón. Se corrigió “Donos de ataque” y un texto con saltos de línea escapados. Doce superficies de texto/accesibilidad recibieron la llamada de traducción que faltaba.

Las pruebas recorren las 300 builds y sus 6.058 descripciones: todas cambian a portugués y superan la comprobación de vocabulario español. Las 180 frases que el catálogo deja iguales están inventariadas en `portuguese-audit-unchanged.json`: vocabulario idéntico en ambos idiomas, nombres compartidos de objetos/runas, marcas, números y abreviaturas del juego. No se consideran errores por conservar el mismo texto.

Esto no certifica al 100% las pantallas ejecutadas, los textos nuevos publicados por usuarios ni los estados dependientes de conexión. El recorrido completo en Android debe comprobar cambios de idioma, diálogos, avisos, errores, vista flotante y restauración de sesión.

## Relaciones de campeones

Las referencias editoriales abarcan los 142 campeones: 141 fichas con counters y sinergias de WildRiftFire, y la ficha de Hwei de WildRiftCore porque sus apartados en WildRiftFire estaban vacíos. Se contrastaron además listas por línea de 138 campeones de WildRiftCounter. Las URLs y categorías están en `matchup-reference-audit.json` y `lane-matchup-reference-audit.json`.

Se retiraron las colas repetidas de relleno. Las ventajas también pueden inferirse invirtiendo una relación explícita de counter. Solo se admiten rivales compatibles con la línea seleccionada; se excluyen el propio campeón, desconocidos y duplicados. Si una relación aparece como ventaja y debilidad, prevalece debilidad. Son recomendaciones editoriales y de criterio táctico, no porcentajes de victoria de enfrentamientos medidos.

Se verificaron 300 perfiles por rol y 900 listas. Ninguna supera doce ni contiene duplicados, al propio campeón o un rival simultáneamente en ventaja y debilidad. Hay 184 listas con menos de seis relaciones y 38 con menos de tres, especialmente en roles secundarios. Los límites son máximos: no se añaden rivales sin justificación para completar el cupo.

## Validación y prueba manual

Pasaron siete pruebas JVM: cinco de selección de botas/runas y límites de sesión, y dos de portugués. También pasó la revisión de sintaxis Kotlin de los archivos modificados, la revisión de las 300 builds/perfiles y `git diff --check`. Las pruebas Compose y de migración Android se actualizaron, pero no se ejecutaron aquí.

1. Elegir portugués y recorrer inicio, acceso, legales, tier list, catálogo, builds, perfil, historial, suscripción, diálogos y avisos; volver a español y repetir.
2. Confirmar que legales conserva únicamente los botones inferiores y que rechazar/atrás mantienen la salida esperada.
3. Abrir consejos en una build normal y en la vista flotante; cambiar botas y volver a las principales; pulsar las runas situacionales.
4. Comprobar límites como invitado, cuenta gratuita y Premium; entrar/salir de sesión sin reiniciar; desplazar las listas Premium.
5. Actualizar desde una instalación con China guardado y verificar que solo aparece Global, incluso sin conexión.

## Reproducir la auditoría de catálogo

`tools/PortugueseAudit.kt` usa el parser de Kotlin y el catálogo real de traducción para inventariar literales, plantillas y campos de JSON. Requiere Kotlin Compiler Embeddable 2.2.10, Kotlin stdlib y org.json 20240303 en el classpath. Se compila junto a `TranslationCatalog.kt`; ejecutar `PortugueseAuditKt <raíz-del-repo> <salida.json>`. La lista de frases sin cambio requiere revisión lingüística; no equivale automáticamente a textos sin traducir.
