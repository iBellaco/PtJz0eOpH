# Coach 1.1.10.351 — build 1067

## Fallo observado y corrección

La ejecución 39985321014 no se aprobó: 42 de 46 secuencias nativas fallaron.
No se fusionó ni publicó ese APK. El catálogo, seguridad, 740 pruebas unitarias,
firma/R8 y revisiones instaladas en español/portugués sí terminaron correctamente.

La captura original no contiene el título Teemo: solo muestra Jugador 5. La
secuencia rival-primero esperaba equivocadamente leer ese nombre como noveno.
Ahora usa una variante explícita que intercambia los últimos retratos y traslada
los píxeles reales del título VI al lado rival; no genera ni inyecta texto OCR.
El décimo campeón, Teemo en el lado correspondiente, mantiene el nombre oculto.
Una prueba comprueba que los píxeles del título son idénticos y los dos retratos
intercambiados cumplen el umbral productivo, sin relajar puntuaciones.

La lectura real también perdió VI en numerosas medidas y títulos a 640 × 360.
Se amplían las bandas de títulos hasta 12× cuando son pequeñas y se añade una
alternativa de contraste invertido. El título intacto conserva prioridad; no se
eliminan posiciones, no se inyectan nombres y siguen siendo obligatorias las dos
lecturas para confirmar. Los resultados del próximo flujo determinarán si basta.

## Lista de dispositivos y límites

Se registran las 32 marcas y todos los modelos solicitados en
`scanner-requested-models.json`. Apple/iOS no puede ejecutar el APK Android.
Los modelos de una marca no heredan compatibilidad ni medidas de otro modelo.
Se añaden 43 perfiles con medidas y fichas públicas primarias: la matriz prevista
asciende a 106 perfiles y 318 variantes, 6360 comprobaciones de retratos/baneos.
El informe genera explícitamente pendientes por modelo; no se afirma haber
verificado especificaciones o variantes que todavía no están confirmadas.

Los perfiles de pantalla son imágenes simuladas, no teléfonos conectados. No
prueban permisos, captura en vivo, notch físico, densidad, rendimiento, consumo,
todas las skins ni cada versión del sistema. Las referencias Red Magic/Poco
conservan su confirmación previa del propietario. No hay S26 físico disponible.
Huawei requiere confirmar Android/servicios disponibles; HarmonyOS NEXT no se
declara compatible. Sony y Nothing incluyen el escenario de panel documentado,
no una afirmación sobre la resolución de captura de todas sus configuraciones.

## Verificación y entrega

Se mantienen todas las barreras de publicación: regresiones, catálogo, seguridad,
geometría, OCR nativo, décimo visual, APK instalado, firma y ofuscación R8.
Las resoluciones idénticas comparten secuencia nativa, reteniendo sus modelos.
No se aprobará el flujo si falta una secuencia o una lectura esperada.
Pendiente: nueva ejecución completa y auditoría medida antes de fusionar.
