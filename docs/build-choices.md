# Coach: alternativas de builds

La bota principal permanece en el selector de nivel 2 después de elegir una alternativa.
El par de nivel 3 sigue la bota seleccionada. Tocar la bota grande después de un cambio
restaura el par principal; con el par principal activo, abre su ficha.

Las builds pueden no tener botas situacionales. Las alternativas se ofrecen con una
condición concreta: daño mágico y control para primera línea, ataques básicos para
tiradores, o frecuencia de habilidades para determinados perfiles de utilidad/jungla.
Mercurio no se añade universalmente. Olaf y Mundo no reciben la recomendación
automática de Mercurio basada únicamente en su condición de primera línea.

Las tres primeras runas secundarias comparten rama; la cuarta pertenece a otra.
Las alternativas indican si pertenecen al grupo de tres o sustituyen la cuarta.
La pantalla de creación valida la página completa antes de guardar.

Ventajas, debilidades y sinergias muestran hasta seis campeones en Gratis y hasta
doce en Premium. Se combinan relaciones existentes compatibles con el rol y se
eliminan duplicados, el propio campeón y contradicciones entre ventajas/debilidades.
No se rellenan listas con campeones arbitrarios para alcanzar el límite.

## Referencias consultadas

- https://www.wildriftfire.com/guide/darius : referencia 7.3a para botas defensivas,
  condición AP/CC, counters y sinergias. La alternativa de Mercurio es condicional.
- https://wildriftcounter.com/champions/darius/ : candidatos fuertes/débiles por rol.
- https://thewildpick.com/counter/darius/ : referencia 7.2b para contrastar counters.
- https://bestbuildwr.com/champions : índice consultado; las fichas no expusieron
  sus builds en el contenido disponible durante esta revisión.
- https://wildriftcore.com/es/champions/ : no accesible durante esta revisión.

Los sitios difieren en parche y criterio. Se usan como referencia, no como una
verificación de las 300 builds ni como garantía de victoria.

## Validación y recorrido para pruebas

Versión 1.1.10.131, código 847.

1. Abrir una build con alternativa de botas, elegirla y volver a la principal.
2. Comprobar el cambio correspondiente de su mejora de nivel 3.
3. Comprobar que Ahri mid no muestra una alternativa defensiva impuesta y que
   la alternativa de Darius explica la condición de daño mágico/control.
4. Confirmar que debajo de los hechizos no aparecen consejos ni comentarios.
5. Crear una página con tres secundarias de una rama y la cuarta de otra;
   comprobar los filtros y el rechazo de páginas inválidas.
6. Revisar las tres listas con Gratis y Premium, también en portugués.

Se ejecutaron cuatro pruebas de reglas Kotlin/JUnit y una validación JVM de las
300 builds y 300 perfiles de rol. No se ejecutó la compilación Android completa
ni una prueba de interacción en dispositivo.

Después de regenerar las builds incluidas, ejecutar:

```sh
python scripts/refine_build_choices.py
```
