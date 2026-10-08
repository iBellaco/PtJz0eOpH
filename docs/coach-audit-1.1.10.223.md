# Auditoría de Coach — 1.1.10.223 (build 939)

## Problemas y cambios

- La fila de Ventaja, Débil y Sinergia imponía 156 dp mínimos por grupo, más de 468 dp en total. Se elimina ese mínimo y el desplazamiento horizontal: los tres grupos reparten el ancho disponible con pesos iguales y 2 dp entre tarjetas. Los retratos se ajustan a su columna, con un máximo de 28 dp, y conservan filas de tres y límites 3/6/12. Los encabezados y el indicador PRO se apilan dentro de cada grupo para evitar desbordamientos.
- Las celdas conservan 48 dp de alto; clickable amplía sus límites táctiles según el mínimo de Compose sin reservar ese ancho para la imagen. Se verifica tanto la geometría visible como el área táctil y la pulsación real sobre el retrato.
- El OCR aliado leía una banda que también podía incluir nombres de invocador. Ahora identifica la primera fila del título, con lectura dirigida y respaldo de la misma región en pantalla completa. Se admiten títulos de línea y nombres completos del catálogo, con prefijos de maestría limitados. Un nombre de jugador situado en la segunda línea, aunque sea exactamente un campeón, no confirma una selección.
- Las líneas observadas pertenecen al puesto del jugador. Al sustituirse el nombre de línea por el campeón, la línea permanece. Un intercambio de campeones tampoco mueve las líneas de los jugadores.
- La asignación aliada deja de usar el rol habitual del campeón o Castigo para presentarlos como líneas observadas. Cuatro líneas distintas determinan la quinta; con menos información, la línea permanece pendiente y el campeón detectado se muestra en el hub con esa indicación.
- Tu puesto solo se confirma con un marcador propio o el nombre configurado coincidente por completo. Se elimina la coincidencia parcial y el nombre fijo anterior. El rol activo guardado ya no crea por sí mismo una identidad confirmada. Tu línea automática procede de la línea observada en ese puesto.
- La sincronización del hub actualiza las líneas observadas sin exigir que las cinco estén resueltas y conserva filas no observadas y selecciones manuales.
- Versión 1.1.10.223 (939), com.Coach, español/portugués y firma persistente. Entrega release con R8.

## Verificaciones previstas

Se amplían las pruebas de relaciones en una pantalla de 330 dp para comprobar que los tres grupos estén totalmente dentro del ancho visible, filas de tres, límites por sesión, áreas táctiles y apertura del nombre mediante pulsación real. Se capturan las relaciones después de desplazar verticalmente su fila.

Se agregan pruebas de nombres de línea en ES/PT, títulos completos del catálogo, nombres de invocador, lecturas contradictorias y marcador de usuario. Las pruebas de reconciliación cubren campeones fuera de su línea habitual, intercambios, información parcial y quinta línea determinada por descarte.

El APK instalado ejecuta el escáner completo sobre una secuencia de líneas, selección, texto perdido e intercambio en ES/PT, con nombres de campeón en la segunda línea de los jugadores para comprobar que no contaminen la selección. También se leen las cinco franjas reales de nombre de las capturas reportadas; los fixtures excluyen los nombres de jugadores. Se mantienen los casos de Vi, Milio y Caitlyn.

Este registro se prepara antes de ejecutar las comprobaciones. El acta incluida con el APK debe reflejar los resultados efectivos. compile_applet no está disponible; se emplea el flujo Gradle y Android del repositorio.

## Archivos

ChampionDetailSheet.kt, ChampionBuildDetails.kt, AdaptiveScreenLayoutEngine.kt, AllyDraftNameReader.kt, AllyDraftReconciler.kt, ChampionNameResolver.kt, DraftVisionScanner.kt, OverlayState.kt, FloatingOverlayContent.kt, FloatingDraftCoachView.kt, recursos draft_observation ES/PT, pruebas de relaciones/nombres/reconciliación/navegación y APK instalado, fixtures de títulos aliados, script de validación instalada, workflow, README.md y app/build.gradle.kts.

## Resumen para testers

Coach 1.1.10.223: Ventaja, Débil y Sinergia deben caber juntas sin desplazamiento horizontal, con campeones de tres en tres. Activar el escaneo antes de seleccionar y comprobar que cada campeón conserve la línea que aparecía en su puesto, incluso si usa una línea poco habitual o intercambia campeón. Tu línea debe coincidir con el puesto identificado como tuyo. Revisar español, portugués y selecciones manuales.

## Commit copiable

fix: ajustar relaciones al ancho y conservar las líneas observadas del draft aliado en Coach
