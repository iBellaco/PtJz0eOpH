# Reglas de versiones y compatibilidad multidispositivo

Este documento forma parte de las instrucciones permanentes del proyecto
Coach. Debe consultarse antes de preparar una compilación, definir una prueba
o interpretar un resultado del escáner.

## 1. Versiones de prueba y producción

Coach utiliza dos tipos de versión:

### Versión de prueba

- Se utiliza para pruebas internas, regresiones y validación previa.
- Conserva la versión base y añade el sufijo `-prueba.N`.
- Ejemplo: `1.1.10.464-prueba.1`.
- La palabra `prueba` debe aparecer junto a la versión visible de la
  aplicación para identificarla claramente.
- Puede incrementarse `N` para distinguir nuevas iteraciones de prueba sin
  convertirlas en una versión de producción.
- No debe activar ni mostrar la comprobación de actualizaciones oficiales.

### Versión de producción

- Se utiliza únicamente cuando la versión ya fue probada y está lista para
  distribuirse.
- Mantiene solamente el formato numérico, sin `-prueba`.
- Ejemplo: `1.1.10.464`.
- Es la única versión que participa en la comprobación oficial de
  actualizaciones.
- Antes de publicar, `versionName` debe ser numérico y `versionCode` debe ser
  ascendente.

### Nombre obligatorio del APK

- Toda APK de prueba debe incluir claramente `prueba` y su iteración en el
  nombre del archivo.
- Ejemplo: `Coach-1.1.10.466-prueba.1.apk`.
- Nunca se debe entregar una APK de prueba con el mismo nombre que la APK de
  producción ni con un nombre ambiguo.
- La APK de producción conserva el nombre normal, por ejemplo:
  `Coach-1.1.10.466.apk`.
- Antes de entregar cualquier APK, comprobar que el nombre del archivo y la
  versión visible dentro de la aplicación indiquen el mismo tipo de versión.

Nunca se debe publicar una compilación que conserve el sufijo `-prueba`.

## 2. Dos funcionalidades de Coach

La aplicación tiene dos modos funcionales que deben mantenerse diferenciados:

### Modo sin overlay

Está destinado a dispositivos o configuraciones donde no se puede utilizar la
interfaz superpuesta sobre Wild Rift. Las funciones compatibles con este modo
deben probarse sin asumir que existe una ventana flotante, una captura
continua o interacción superpuesta.

### Modo con overlay

Está destinado a dispositivos Android donde Wild Rift y la interfaz superpuesta
funcionan correctamente, incluyendo los permisos de captura y superposición
necesarios. El escáner automático pertenece a este modo cuando se prueba sobre
la partida.

## 3. Regla obligatoria para las pruebas del escáner

Las pruebas del escáner automático deben ejecutarse exclusivamente con
dimensiones y configuraciones que soporten simultáneamente:

1. La ejecución de Wild Rift.
2. La captura de pantalla requerida.
3. La interfaz superpuesta de Coach.
4. La visualización estable del overlay durante toda la secuencia del draft.

No se deben usar como evidencia del escáner las dimensiones que solo soporten
la imagen de Wild Rift pero no el overlay, ni las que no permitan ejecutar
Wild Rift correctamente. Esas dimensiones pueden servir para pruebas aisladas
del modo sin overlay, pero no para validar posiciones, baneos, slots, títulos,
preselecciones o selecciones del escáner superpuesto.

## 4. Criterios multidispositivo

- La geometría debe calcularse a partir de las dimensiones reales de la captura,
  el viewport y los insets disponibles.
- No se deben introducir coordenadas específicas de una marca o modelo.
- Cada perfil usado para validar el overlay debe indicar que es compatible con
  Wild Rift y con la superposición antes de incluirse en la matriz del escáner.
- Las pruebas deben cubrir cambios de relación de aspecto y tamaño dentro del
  conjunto de dispositivos compatibles, manteniendo la misma secuencia visual.
- Una prueba que no pueda mostrar simultáneamente el juego y el overlay debe
  clasificarse como modo sin overlay y no puede aprobar la compatibilidad del
  escáner superpuesto.

## 5. Regla para futuras modificaciones

Antes de añadir una dimensión a las pruebas del escáner, comprobar primero la
compatibilidad de Wild Rift y del overlay. Si no cumple ambas condiciones, no
se incorpora a la matriz de validación del escáner; se documenta, si procede,
como prueba independiente del modo sin overlay.
