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
Una prueba comprueba que los píxeles del título son idénticos, que los retratos
conservan su identidad y que el décimo cumple el umbral productivo y necesita dos
frames. El noveno se confirma por texto; no se exige aceptarlo visualmente.

La lectura real también perdió VI en numerosas medidas y títulos a 640 × 360.
Se amplían las bandas de títulos hasta 12× cuando son pequeñas y se añade una
alternativa de contraste invertido. El título intacto conserva prioridad; no se
eliminan posiciones, no se inyectan nombres y siguen siendo obligatorias las dos
lecturas para confirmar. Los resultados del próximo flujo determinarán si basta.

## Lista de dispositivos y límites

Se registran las 32 marcas y todos los modelos solicitados en
`scanner-requested-models.json`. Apple/iOS no puede ejecutar el APK Android.
Los modelos de una marca no heredan compatibilidad ni medidas de otro modelo.
Se añaden 47 perfiles con medidas y fichas públicas primarias, incluidos Pixel 3 XL,
POCOPHONE F1, Mi 8 Pro y Zenfone 5 ZE620KL de la lista de notch clásico.
La matriz incluye además ocho escenarios de márgenes de recorte/navegación:
118 perfiles, 354 variantes y 7080 comprobaciones previstas de retratos/baneos.
Esos márgenes son sintéticos (5 % lateral y 3 % de navegación), a izquierda y
derecha, y no se atribuyen como dimensiones reales del notch de cada modelo.
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
La primera repetición detectó que AGP 9 no incorporaba el helper Kotlin compartido
mediante java.srcDir. Se corrige a AndroidSourceSet.kotlin.directories, según la
documentación de Kotlin integrado; no se omite la prueba que lo utiliza.
La siguiente repetición encontró similitud 0.756 para el retrato trasladado de Vi:
se exigía indebidamente el umbral visual del décimo a ese noveno pick por texto.
Se conserva la comprobación de identidad y del título original; la inferencia
visual y su umbral de 0.80 siguen siendo obligatorios para el décimo Teemo.

## Resultado de 39994943410 y segunda corrección

La matriz de imágenes aprobó 118 perfiles, 52 medidas, 354 variantes y 7080
comprobaciones. Se aprobaron 743 pruebas unitarias sin omisiones, la firma/R8
(1383 clases renombradas), 39 pantallas instaladas en español y 41 en portugués.
El APK no incorporó los archivos de la matriz. Se revisaron las imágenes de
Jelly Star, Pixel 3 XL, margen 854 × 480 y pantallas portuguesas.

Sin embargo, la secuencia nativa completa aprobó solo 29/63 casos. No se fusionó
ni publicó: persistían VI sin lectura, títulos pequeños mal leídos y un caso Vex
con confirmación tardía. La auditoría parcial publicada no certifica esta fase.

La inspección de los atlas mostró una insignia de maestría parcialmente borrada
junto a VI. La supresión ahora localiza componentes dorados altos en lugar de
recortar un ancho fijo, manteniendo las letras doradas pequeñas y el original.
Los títulos no resueltos se vuelven a leer en una imagen pequeña independiente,
con píxeles y exclusiones conservados; no se incrementa la confirmación por
obtener dos respuestas OCR dentro del mismo fotograma.

Se normalizan únicamente lecturas completas observadas (NDCTURNE, VOLIREAR,
VOLIEAR y títulos completos de líneas comprimidos); no se añaden coincidencias
parciales, apodos, abreviaciones ni inferencia de línea por campeón/posición.

Se añade una comprobación nativa preliminar de los tamaños problemáticos y las
dos referencias conocidas, con etapas 0/9. Su modo es explícitamente no
publicable; el informe final rechaza usarlo como sustituto de los 63 casos con
etapas 0–9. Así una lectura aún incorrecta se detecta antes de lanzar toda la
matriz. Las barreras completas de publicación y los umbrales no se reducen.
Pendiente: repetir toda la validación con esta corrección.
