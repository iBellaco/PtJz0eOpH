# Contrato de reconocimiento de la selección

Confirmado por el propietario el 23 de octubre de 2026. Leer antes de modificar
la captura, el visor, la calibración, el reconocimiento o sus pruebas.

## Información que se observa

Los cinco slots aliados, cinco rivales y cinco baneos de cada lado conservan
su distribución lógica. Se transforman respecto de la ventana del juego, no
respecto de barras negras, recortes o ventanas flotantes. El visor y el escáner
deben consumir exactamente la misma geometría. Las dimensiones de una pantalla
no demuestran cuál es la resolución efectiva de captura en ese teléfono.

Los fondos, el campeón central, las skins, los retratos y los campeones baneados
cambian. Nunca usar el fondo ni el campeón central como identidad de un slot.

## Selecciones 1–9: solo texto

Leer el título completo del campeón en su banda, sin coincidencias de subcadenas
ni abreviaturas. Una insignia de maestría puede precederlo. El nombre de invocador
está debajo: incluso si coincide con un campeón, no es el título seleccionado.

En aliados, el título antes del pick indica la línea; después cambia al campeón.
Conservar la línea observada de ese mismo slot. No sustituirla por la afinidad
habitual del campeón, por su posición ordinal ni por una estimación estadística.

En rivales, la línea previa no es visible. Tras el pick, el título identifica al
campeón. Su línea primaria/flex puede estimarse con estadísticas y composición,
pero esa estimación no debe presentarse como una línea observada o cierta.

Exigir la confirmación temporal productiva. Releer una imagen de otra forma no
equivale a recibir otro fotograma. Un retrato visible puede ser una preselección:
no confirma ninguno de los primeros nueve picks.

## Décima selección: visual y condicionada

| Primera selección | Décima selección visual |
| --- | --- |
| Aliados | Último rival |
| Rivales | Último aliado |

Activarla solo después de nueve campeones distintos confirmados por texto.
Mantener los umbrales y la confirmación temporal visual; no rellenar una lectura
faltante con el retrato. Una imagen con ocho títulos sigue siendo de ocho aunque
muestre diez retratos. Las capturas no prueban el orden temporal de una partida:
las pruebas secuenciales deben declarar qué etapas son simuladas.

## Baneos

Conservar las diez posiciones, aunque no se banee en alguna. Un círculo de
omisión no es un campeón, y una observación desconocida no es un baneo confirmado.
No desplazar los restantes para cerrar un hueco. No inventar identidades a partir
del fondo ni del vecino; las exclusiones del visor también se aplican aquí.

## Evidencias y límites

Red Magic y POCO funcionan en pruebas físicas reportadas por el propietario.
Las pruebas de imágenes redimensionadas y las de reconocimiento nativo instalado
en emulador se informan por separado. No equivalen a probar físicamente Samsung,
Redmi, Xiaomi ni los otros modelos que comparten esas medidas. No hay S26 físico
disponible. El APK Android no es una aplicación iOS.

Capturas reales adicionales: `draft-swain-eight.jpg` (ocho títulos, APOYO aún
visible); `draft-kaisa-nine.jpg` (nueve títulos, CALLE DEL BARÓN aún visible y
segundo baneo aliado omitido); `draft-mel-nine.jpg` (nueve títulos y décimo rival
sin título). Todas están en `app/src/test/resources/portraits/`.
