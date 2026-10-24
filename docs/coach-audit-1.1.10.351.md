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

## Comprobación preliminar de 40004549460

Se aprobaron de nuevo las 354 variantes y 746 pruebas unitarias sin omisiones,
la compilación, firma persistente y R8. El APK de esta ejecución tiene SHA-256
`bf717add778abada04b032840bc5effa0e36e0555ee95cfbaeb5c4cc32e13046`.
La prueba preliminar nativa aprobó 7/15 casos y falló 8/15: principalmente VI,
además de Volibear a 640 × 360. No se aprobó ni publicó ese APK.

El atlas conservaba alas pálidas de la insignia tras redimensionar: la máscara
ahora incluye el dorado mezclado, con una regresión de los píxeles reales a
1920 × 869 y conservación exacta de VI. La relectura añade una alternativa de
contraste local binario para separar los trazos del fondo cambiante; no dibuja
letras ni utiliza retratos para los primeros nueve. Se guarda también el atlas
y la respuesta de esta relectura cuando falla una prueba nativa.
Se unen fragmentos completos del mismo renglón, ordenados de izquierda a derecha
(por ejemplo V + I), sin incluir el apodo inferior ni resolver títulos ambiguos.
Pendiente: verificar esta nueva corrección en el APK instalado y toda la matriz.

## Comprobación preliminar de 40011065704

Se aprobaron 748 pruebas unitarias sin omisiones y la matriz de 354 variantes.
La comprobación nativa preliminar aprobó 9/15 casos y falló 6/15. No se publicó.
La relectura diagnóstica pudo leer VI con más altura donde la relectura aislada
y muy ancha no obtenía texto. Se compactan ambos atlas para minimizar su lado
mayor, manteniendo los mismos píxeles, máscaras y coordenadas internas, y evitando
reducir excesivamente los títulos cortos dentro del detector. Una regresión
comprueba el tamaño óptimo y las regiones de 1–10 títulos no resueltos.

Se incorporan 40 perfiles documentados adicionales, con variantes regionales
separadas para Redmi 9, y las medidas oficiales de S26/S26 FE y S26+/Ultra.
Se añaden medidas antes ausentes de A20, Mate 30 Pro, Mate 50 Pro, P20/P20 Pro,
vivo Y11 2019, Moto G7/G7 Power y Honor X8a; MEIZU 21 y OUKITEL WP62 amplían
las referencias de marcas. La ampliación prevista es de 158 perfiles y 474
variantes (9480 comprobaciones); solo la nueva ejecución puede aprobarlas.
No se presentan los modelos aún pendientes como probados ni los perfiles de
pantalla como teléfonos conectados. Se conservan todas las barreras nativas.

La revisión de las medidas añade otras seis referencias: Hot 10 Play G35
(1640 × 720), Mate 60 Pro (2720 × 1260, únicamente pantalla; no certifica
HarmonyOS NEXT), CAT S62 Pro, iQOO Z9x 5G, Alcatel 1S 2020 y CMF Phone 1.
Las fuentes de estas seis filas distinguen la ficha oficial de iQOO de las
mediciones/revisiones originales de unidades de DXOMARK, Notebookcheck y
YugaTech; no se atribuyen sus pruebas físicas a Coach. Así quedan previstas
164 referencias, 492 variantes y 9840 comprobaciones. Las medidas nuevas se
incluyen en el plan preliminar no publicable y en la secuencia nativa completa.

## Resultado de 40018536124 y capturas del propietario

Se midieron 164 perfiles, 63 medidas, 492 variantes y 9840 comprobaciones de
identidad aprobadas; también 749 pruebas unitarias sin errores ni omisiones.
La compilación, firma, R8 y revisiones instaladas en español/portugués aprobaron.
La comprobación nativa preliminar aprobó 24/26 escenarios: fallaron 640 × 360
y 854 × 480 con margen derecho/inferior. No se publicó ni fusionó ese APK.

El diagnóstico muestra que el relleno de un píxel de la máscara a resolución
pequeña puede dañar un título adyacente y que la banda conserva el inicio del
nombre de invocador. Se traslada la máscara a la copia ampliada y se añade una
última lectura aislada de la parte de título para los slots aún no resueltos.
No se acepta V como Vi, no se dibujan nombres y las relecturas no cuentan como
fotogramas nuevos. Falta que el reconocimiento nativo valide esta corrección.

Se guarda el contrato completo en `docs/draft-recognition-contract.md`, enlazado
desde las instrucciones maestras. Se conservan las tres nuevas capturas reales,
sus hashes y títulos transcritos independientemente. La matriz nativa completa
debe leerlas en cada geometría, bloquear el décimo con ocho títulos y reconocer
el décimo Teemo rival con nueve. Se comprueba además el baneo aliado omitido.
El propietario confirmó que el último aliado de la captura de Kai’Sa es Poppy:
también se exige su reconocimiento visual como décima aliada con nueve títulos.
Esto no sustituye la secuencia completa de ambos órdenes ya exigida.

Solo este documento de resultados se añade a la lista exacta de archivos de
entrega que permiten reutilizar un APK previamente aprobado. Contrato, reglas,
capturas, conjuntos de datos, pruebas y flujos siguen obligando a validación nueva.

## Resultado de 40036800896 y relectura de títulos completos

La fuente bf7684e9 aprobó 164 perfiles, 63 medidas, 492 variantes, 9840
comprobaciones de identidad y 751 pruebas unitarias sin fallos ni omisiones.
Pasaron la compilación firmada, R8 (1383 clases renombradas), el renderizado y
41 pantallas del APK instalado en portugués, con cero textos españoles detectados.
El APK tiene SHA-256 `17f908ca792a73cc4d8ac14a031b820586f8463ce2c093badf70032b61ea7d2a`.
No se aprueba para entrega: el reconocimiento nativo pasó 23/26 escenarios.

Las 12 comprobaciones adicionales de las tres nuevas capturas sí aprobaron:
ocho títulos sin activar el décimo, nueve con Poppy aliada y nueve con Teemo rival.
Fallaron el título Vi en una referencia 1920 × 864, Vi/Volibear en 640 × 360
y Vi rival en 854 × 480 con márgenes. La prueba terminó todos sus casos en
1346 segundos; no fue un agotamiento del tiempo ni se omitieron escenarios.
La revisión instalada en español falló antes de iniciar la app por una imagen
de emulador descargada como archivo ZIP inválido; se solicitó su repetición.

Los recortes muestran ambos trazos de VI, pero demasiado pequeños dentro de una
banda con amplias zonas vacías. Se restaura la alternativa de insignia previa a
la ampliación, que no tenía la regresión observada en Volibear y la referencia
conocida. La última relectura se centra horizontalmente en posibles trazos,
con relleno amplio y manteniendo toda la altura y los píxeles originales.
Los límites verticales de aceptación excluyen ruido superior y el apodo inferior;
no se generan letras ni se acepta V como si fuera VI.

Los slots uniformes o completamente enmascarados no disparan relecturas extra.
Se añaden pruebas que conservan los nombres completos del catálogo en blanco
y dorado, los dos glifos reales de VI en las medidas problemáticas y las máscaras.
Se conserva la confirmación en dos fotogramas, las tres capturas y todos los
casos del plan. La instrumentación transmite y guarda su progreso al recibirlo;
si falla o se agota el tiempo, sus datos parciales no se marcan como aprobados.
Pendiente: nueva validación completa antes de fusionar o publicar.

En el reintento de la revisión española se superó la descarga del emulador.
Uno de los cinco casos detectó un nodo de contraseña obsoleto tras reabrir el
diálogo nativo: la escritura se ejecutó antes de estabilizar el foco. La prueba
ahora toca explícitamente el campo, espera el fotograma y comprueba el texto
introducido antes de continuar. Se mantienen las dos confirmaciones, contraseña
obligatoria, bloqueo de envíos duplicados y comprobaciones de error; no se cambia
la autorización de la aplicación ni se ejecutan eliminaciones reales.
