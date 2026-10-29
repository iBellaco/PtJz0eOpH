# Coach 1.1.10.413 — build 1129

## Fallos comprobados

La validación 40790640943 de 1.1.10.412 falló en dos trabajos:

- core-and-rendered (123175713113):
  TenthPickRegressionTest.previewChangesReplaceOnlyTheTrackedTenthChampionOnEitherTeam
  esperaba Vi en la quinta fila del Hub, pero encontraba own3. La prueba creaba
  los nueve campeones sin roles y exigía posiciones fijas incluso en el rival.
  Se ejecutaron 787 pruebas, con una fallida y una omitida; fail-fast impidió
  considerar aprobados los casos posteriores.
- installed-spanish (123198375762): los dos casos instalados de Vi reconocían
  Vi correctamente, pero fallaban al conservar a Darius en TOP. Las cuatro
  identidades rivales previas se construían con Champion(id), cuyo rol por
  defecto es MID. La estimación completa recibía datos distintos del catálogo
  real y colocaba a Caitlyn en la primera fila.

La matriz completa del escáner, seguridad, compilación release, auditoría
portuguesa instalada, preflight de títulos y las 16 particiones de secuencias
OCR instaladas terminaron correctamente. La entrega quedó bloqueada por las
pruebas anteriores; no se presenta el APK como validado.

## Corrección

No se cambia el algoritmo productivo del escáner, la política de roles ni los
umbrales para acomodarlos a datos de prueba incompletos.

Los casos que ejercitan asignación de roles cargan campeones del catálogo real.
La regresión del cambio de retrato distingue las dos reglas del contrato:
conservar las filas aliadas observadas, pero recalcular los roles rivales ocultos.
Comprueba explícitamente las cinco identidades en orden esperado, el seguimiento
Vi → Wukong, informes repetidos, rechazo de un pick anterior como nueva décima
selección, las nueve identidades previas, el otro equipo, diez campeones distintos
y el bloqueo manual en la posición actual del último pick.

Se revisan dos expectativas próximas en la misma clase antes de volver a ejecutar
el conjunto: los nombres rivales actuales y Yunara también usan roles reales;
la confianza de Vi en jungla debe ser una estimación de 90, no certeza de 100.
Los dos casos instalados mantienen las imágenes originales, la confirmación de
dos fotogramas, Vi en jungla y las nueve posiciones anteriores. Añaden conteo de
diez identidades distintas y comprobación de la confianza de rol estimada.

## Pruebas tempranas en fix-error

- Se añade toda TenthPickRegressionTest al trabajo JVM existente, no únicamente
  el primer método que falló.
- Se añade un trabajo obligatorio paralelo que compila ambos APK release con
  R8 y firma efímera de pruebas, los instala en el emulador y ejecuta únicamente
  los dos casos de Vi que fallaron. No publica APK ni modifica la firma de entrega.
- El ejecutor admite --vi-only exclusivamente para esa comprobación enfocada;
  exige ambas pruebas exactas, cierre normal y ausencia de fallos u omisiones.
  Sin esa opción sigue exigiendo toda la clase instalada. La validación completa
  no se modifica ni se sustituye por el subconjunto.
- La matriz opcional de regresiones release y el trabajo de patrocinio permanecen
  estructuralmente idénticos; solo se reutilizan pasos mediante anclas YAML.

## Archivos modificados

- app/src/test/java/com/example/TenthPickRegressionTest.kt.
- app/src/androidTest/java/com/example/DraftRivalNameInstalledTest.kt.
- tools/verify-draft-rival-names-device.py.
- tools/test_draft_rival_device.py.
- .github/workflows/fix-error.yml.
- app/build.gradle.kts.
- docs/coach-audit-1.1.10.413.md.

## Verificaciones y límites

Se compiló ConfirmedLastPickHud, TenthPickHudPolicy y las políticas de roles y
reconciliación productivas con el catálogo real y adaptadores mínimos de estado
y snapshots. La reproducción confirmó Caitlyn en TOP con el fixture antiguo y
aprobó el fixture corregido, ambos lados, repetición, cambio de campeón, bloqueo,
nueve identidades, eliminación de previews viejos, Yunara y confianza estimada.
No sustituye Robolectric ni una prueba instalada Android.

Pasaron diez pruebas del ejecutor de instrumentación, la prueba del reporte
JUnit, validaciones de recursos ES/PT, contenido, builds generadas y
git diff --check. Se verificó estructuralmente que siguen intactos los trabajos
opcionales y de patrocinio y que los dos casos instalados son obligatorios en fix.

No hay SDK Android local. Las clases completas y los casos instalados nuevos
quedan pendientes de Actions; no se afirma un verde nuevo ni una prueba física.

## Entrega

Solo fix-error. Coach-validacion y main no se modifican. Se espera el aviso del
propietario antes de promover el mismo código aprobado, sin monitoreo continuo.

Resumen para testers: Coach 1.1.10.413 actualiza las pruebas de última selección
para usar roles reales y distinguir filas aliadas observadas de roles rivales
estimados. Verificar cambios del último campeón, Vi en jungla y bloqueos manuales.
No cambia el reconocimiento de la aplicación; pendiente de validación Android.

Commit: Actualizar pruebas del último pick con roles reales y verificar Vi en release
