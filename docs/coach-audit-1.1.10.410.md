# Coach 1.1.10.410 — build 1126

## Fallo comprobado

La ejecución 40711331234 de fix-error falló en el trabajo 122897724565.
La compilación terminó y se ejecutaron 138 pruebas: 137 aprobadas y una fallida,
videoHeaderPortraitsConfirmWithoutChangingTenthPickAndResetForNextDraft.
Las comprobaciones previas de catálogo e idiomas y el trabajo de solicitudes de
patrocinio terminaron correctamente.

El fixture real bans/poco-header.jpg perdía Lee Sin y Rammus. Una reproducción
local instrumentada mostró que el reconocimiento sí devolvía sus identidades;
se perdían al convertir los cinco círculos en posiciones de una cuadrícula
uniforme. El hueco entre Karma y Lee Sin es mayor que los anteriores. Tras el
refinamiento de recortes, la tolerancia calculada con la mediana del espaciado
rechazaba los dos últimos círculos.

## Corrección

Cuando existen cinco observaciones de una fila, usar directamente sus cinco
centros medidos en orden horizontal. No extrapolarlos desde un paso uniforme.
Una fila completa también sustituye la geometría provisional aprendida de una
lectura parcial. Si faltan observaciones, se conserva la recuperación anterior
y las posiciones reservadas; un hueco no se comprime ni inventa una identidad.

Se mantienen las correcciones de Mel/Darius, primera selección, roles rivales,
catálogo y builds. No se cambia ningún umbral ni se retira ninguna prueba. El
ajuste es compartido entre dispositivos y no añade coordenadas por marca.

## Archivos

- app/src/main/java/com/example/service/screen/BannedPortraitScanner.kt:
  asociación de posiciones completas por centros observados.
- app/src/test/java/com/example/BannedPortraitScannerTest.kt: caso determinista
  de fila parcial → completa con espaciado desigual y oclusión posterior; se
  refuerza la prueba del fixture real para exigir las cinco identidades aliadas
  en su orden exacto, además de su aserción original.
- app/build.gradle.kts: incremento de versión y build.
- docs/coach-audit-1.1.10.410.md: este reporte.

## Verificación y límites

La reproducción local empleó las funciones de reconocimiento de cabecera,
PortraitMatcher, BanDetection y BannedPortraitScanner, con el fixture y los
retratos reales. Se usó un adaptador de imágenes de escritorio en lugar del
runtime gráfico Android: no es una prueba instalada ni física. Antes del cambio
se obtuvo [camille, vladimir, karma, null, null]; después se obtuvo
[camille, vladimir, karma, lee_sin, rammus]. También se comprobaron la transición
de tres observaciones a cinco y la conservación ante una captura sin observaciones.

Pasaron las validaciones locales de recursos, contenido y builds generadas, y
git diff --check. Las 138 pruebas Android anteriores y la nueva prueba quedan
en el mismo flujo fix-error; no se afirma que la nueva ejecución haya pasado.
No hay APK nuevo validado todavía. La comprobación física en POCO sigue pendiente.

## Entrega

Solo fix-error. No se modifica coach-validacion ni main. El propietario avisa
del resultado; no se mantiene monitoreo continuo.

Resumen para testers: Coach 1.1.10.410 corrige baneos omitidos cuando los cinco
círculos tienen separaciones diferentes. Revisar que Lee Sin y Rammus permanezcan
en su posición sin alterar los demás baneos.

Commit: Conservar las posiciones reales de los cinco baneos detectados
