# Auditoría de Coach — 1.1.10.221 (build 937)

## Cambios

- Escribir a privacidad inicia directamente ACTION_SENDTO con el correo y asunto localizado; deja de depender de resolveActivity, que puede ocultar aplicaciones por las restricciones de visibilidad de Android. Si falta una aplicación compatible o el sistema bloquea la apertura, copia el correo y muestra instrucciones en ES/PT.
- Se retira el botón que abre la página web desde el diálogo legal y se ajusta el texto para indicar la solicitud por correo. La eliminación dentro de la aplicación conserva su flujo.
- Ventaja, Débil y Sinergia se colocan en una sola fila con 4dp entre tarjetas, 4dp de relleno y 2dp entre retratos. Cada grupo mantiene filas de tres campeones y los límites actuales 3/6/12. La fila admite desplazamiento horizontal en pantallas estrechas para conservar áreas de toque de 48dp, sin apilar grupos ni forzar espacios grandes.
- Los consejos generados de runas, hechizos y botas ya no insertan de nuevo la descripción o pasiva del catálogo. Los objetos conservan la decisión de compra, amenazas y consejos distintos de la pasiva; se evita repetir el mismo consejo entre secciones.
- El filtrado normaliza mayúsculas, acentos, formato y puntuación, retira frases copiadas o casi iguales y conserva las decisiones tácticas adicionales. Se aplica también al renderizar el consejo del elemento elegido.
- Se conserva la identidad Coach, com.Coach, la firma de entregas y la compilación release con R8.

## Verificaciones

Se amplían las pruebas existentes de contacto legal con apertura directa, correo de destino, ausencia del botón web y fallback por aplicación ausente/bloqueada en ES/PT. Las pruebas de builds verifican la posición horizontal de las tres tarjetas, filas de tres, límites por sesión y apertura del nombre sin navegar. Las pruebas de consejos verifican duplicados, texto portugués, conservación de decisiones y cobertura del catálogo de runas/hechizos.

Este registro se prepara antes de ejecutar la validación de entrega. El acta final y las ejecuciones de la versión deben indicar qué comprobaciones terminaron efectivamente. No se declara aprobado un APK antes de comprobar compilación, firma, ofuscación e instalación. compile_applet no está disponible; se usa el flujo Gradle/Android SDK del repositorio.

## Archivos

README.md, app/build.gradle.kts, PrivacyPolicyDialog.kt, PrivacyContact.kt, ChampionBuildDetails.kt, ChampionDetailSheet.kt, BuildElementAdvice.kt, account_deletion.xml ES/PT, AccountDeletionRenderedTest.kt, BuildCoachingRenderedTest.kt, BuildElementAdviceTest.kt y BuildElementAdviceCoverageTest.kt.

## Resumen para testers

Coach 1.1.10.221: comprobar que Escribir a privacidad abre el correo o copia la dirección con aviso si no hay una app compatible; que no aparece el acceso web; que Ventaja, Débil y Sinergia comparten una fila compacta con campeones de tres en tres; y que los consejos aportan decisiones sin repetir la descripción. Revisar español y portugués.

## Commit copiable

fix: corregir contacto de privacidad, compactar relaciones y evitar consejos repetidos en Coach
