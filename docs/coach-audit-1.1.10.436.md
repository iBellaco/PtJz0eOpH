# Coach 1.1.10.436 — build 1152

- El contenido Draft del Hub coloca los avisos y los baneos en una columna, evitando que los avisos de certeza y líneas estimadas se dibujen encima de los retratos.
- Desde ocho selecciones se pospone la lectura global de metadatos y el trabajo adicional de baneos. Los títulos mantienen pasadas rápidas con recuperación exhaustiva de nombres cada ocho pasadas, sin bajar umbrales ni confirmar dos veces la misma captura.
- Se inicia la observación del décimo retrato en el fotograma que confirma el noveno título. Su confirmación sigue requiriendo otro fotograma. Los campeones sin línea observada cuentan para la secuencia, sin inventar su rol.
- Corrige dos causas del rojo de validación: una prueba de reglas esperaba permitir el canje retirado y las pruebas del visor dependían de una conexión real para autorizar su acceso. Ahora estas últimas inyectan explícitamente su política permitida, manteniendo el cierre seguro en producción.
- Las pruebas del visor y las reglas completas pasan también por fix, para detectar estos fallos antes de la validación larga.

Verificación: pruebas de reglas y patrocinios en emulador aislado; comprobaciones de contenido de parche y recursos localizados. Prueba de cadencia añadida para los picks 8–10. Compilación Android, pruebas Compose y medición de latencia en un teléfono pendientes del flujo fix y de la comprobación física. No se garantiza recuperar un campeón desaparecido antes de dos observaciones válidas.
