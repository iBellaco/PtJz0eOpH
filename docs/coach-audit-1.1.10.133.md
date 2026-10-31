# Auditoría Coach 1.1.10.133 (849)

- Restaurados los iconos de objetos situacionales: las 300 builds incluidas conservan cuatro opciones. El selector completa hasta dos cuando faltan opciones válidas en una build personalizada.
- Eliminados los consejos particulares de objetos, runas y hechizos en la vista de build. Se conserva el consejo general desplegable y la información del catálogo al tocar un icono.
- Las alternativas de runas solo sustituyen la clave o la cuarta secundaria. No se generan automáticamente por diferencias entre páginas. De 300 builds, 286 no tienen alternativas y 14 conservan una alternativa con una condición específica.
- Premium muestra hasta 12 recomendaciones por categoría en filas de tres, usando el desplazamiento general de la pantalla. Invitados: tres; cuentas gratuitas: seis. Las relaciones de ventaja y debilidad no se solapan. No se completan listas con relaciones inventadas: 184 de las 900 listas por rol tienen menos de seis relaciones disponibles y 38 tienen menos de tres.
- Eliminada la segunda cabecera del filtro de líneas en la tier list.
- Añadidas 59 traducciones completas de datos Kotlin que escapaban a la extracción anterior, además de los textos nuevos de esta revisión.

## Comprobaciones

- Siete pruebas JVM de reglas de builds y permisos: correctas.
- Validación de 300 builds y 300 perfiles por rol: correcta.
- Auditoría de localización ampliada reproducible: tools/PortugueseAudit.kt acepta --wide. Revisa 1.155 frases Kotlin con indicadores de español y 4.256 frases totales incluyendo datos JSON. Los 29 resultados sin cambio son 15 expresiones internas de búsqueda/formato y 14 nombres compartidos entre idiomas; fueron revisados manualmente.
- Extracción de llamadas de interfaz: 1.573 frases Kotlin, 4.735 frases totales. Los 180 resultados sin cambio incluyen nombres, parámetros y textos no españoles; no equivalen a 180 traducciones faltantes.
- Sintaxis Kotlin de seis archivos modificados: correcta.
- Pruebas de localización de todas las descripciones incluidas y de controles: correctas.

## Pendiente

Este entorno no dispone del SDK Android. Falta compilar la APK y recorrer visualmente las pantallas con portugués seleccionado, incluyendo cambios de idioma sin reiniciar y contenido remoto o creado por usuarios. La auditoría estática no certifica una traducción visual del 100 %. También falta ampliar las relaciones verificadas de los perfiles que no llegan a 12 recomendaciones.
