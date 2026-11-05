# Coach 1.1.10.480 — build 1196

## Corrección de límites de viewport en tabletas

La prueba instalada de nueve títulos falló solo en la superficie sintética
1024 × 768: confirmó los cinco aliados y ninguno de los cuatro rivales.
El detector encontraba las franjas laterales de 40 y 60 píxeles, pero descartaba
el rectángulo 924 × 768 porque su proporción era inferior a 1,5. Al volver a
usar el ancho completo desplazaba la banda rival hacia su retrato.

- `DraftFrameGeometry.kt`: admite áreas observadas apaisadas desde proporción 1,
  conservando los límites de búsqueda y el rechazo de capturas vacías.
- `DraftMultideviceBoxesTest.kt`: verifica la ruta detector → geometría → columnas
  y que los cuatro títulos rivales quedan dentro de sus regiones.
- Se mantiene intacta la prueba instalada que encontró el fallo, incluyendo sus
  seis dimensiones y la exigencia de nueve títulos sin aceptar el invocador.
- `fix-error.yml`: permite seleccionar solo la suite instalada en español;
  recompila el nuevo código y mantiene las comprobaciones previas y regresiones
  JVM necesarias. No reutiliza un APK anterior como evidencia del nuevo código.

## Verificación

La ejecución anterior aprobó compilación release, regresiones JVM, Vi, secuencias
Red Magic/Xiaomi y títulos cortos. La suite de nombres instalada tuvo 16 pruebas
correctas y una fallida. La auditoría posterior de español no llegó a ejecutarse.
La corrección requiere una nueva ejecución; no se da por verificada todavía.
Pasaron localmente 15 comprobaciones de configuración de pruebas y 11 del
ejecutor de la suite instalada. `git diff --check` no encontró errores.
El entorno local carece del SDK Android. No se ha demostrado latencia menor de
150 ms ni compatibilidad física con todos los celulares y tabletas.
