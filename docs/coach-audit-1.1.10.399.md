# Coach 1.1.10.399 — build 1115

## Solicitud y causa comprobada en código

El propietario reconfirma la secuencia de títulos aliados, línea antes del pick,
campeón después, invocador debajo e insignia de maestría opcional. Se conserva en
AGENTS.md y docs/draft-recognition-contract.md para futuras intervenciones.
Primera selección aliada implica décimo rival; primera rival, décimo aliado.

La ruta namesOnly del bucle automático evitaba el OCR de pantalla completa pero
todavía ejecutaba toda la cadena de recuperaciones antes de devolver cualquier
campeón al Hub. Los slots pendientes podían provocar relecturas por escalas,
colores, títulos cortos y contexto aunque otros títulos ya fueran legibles.
Esto es trabajo bloqueante comprobado en código; no es una medición física del
tiempo reportado por el propietario.

## Cambios

- FloatingOverlayContent solicita quickTitlePass en las pasadas rápidas.
- DraftVisionScanner limita ese modo al atlas original y un reintento agrupado:
  máximo dos solicitudes OCR. Mantiene las recuperaciones completas en las
  pasadas periódicas y las llamadas existentes de diagnóstico.
- SlotTitleConfirmation no considera la ausencia en una pasada limitada como
  un fallo exhaustivo. Así, los nombres difíciles pueden reunir dos lecturas
  completas aunque entre ellas haya pasadas rápidas sin resultado. Esas pasadas
  vacías nunca confirman; una lectura distinta reemplaza la candidatura. Dos
  fallos exhaustivos o el reinicio la eliminan. No se reduce la confirmación a
  un solo fotograma ni se usan retratos para los primeros nueve picks.
- app/build.gradle.kts incrementa versión y build.
- LiveTitleConfirmationTest verifica las dos confirmaciones, independencia por
  equipo/slot, continuidad entre pasadas y descarte de evidencias anteriores.
- DraftRivalNameInstalledTest añade pasadas rápidas en español y portugués:
  lectura de líneas, transición a campeones, invocador JINX debajo del título,
  actualización del Hub por línea observada e intercambios. Comprueba máximo
  dos solicitudes por pasada y registra duración, sin inventar un umbral físico.
- tools/verify-targeted-red-magic.py ejecuta ambas regresiones nuevas; el flujo
  fix-error.yml incorpora las pruebas de confirmación y títulos estables.

## Verificación y límites

JUnit local con el compilador Kotlin disponible: 3 pruebas nuevas aprobadas.
Auditoría Python de secuencias: 10 pruebas aprobadas. git diff --check aprobado.
El intento local de Gradle no pudo iniciar las pruebas Android:
`SDK location not found`. Este entorno no tiene SDK configurado; por ello las
3 pruebas anteriores se compilaron y ejecutaron directamente con Kotlin/JUnit.
La compilación Android y las pruebas instaladas quedan sujetas al resultado
real de fix-error; no se presentan como aprobadas antes de que termine.

Las secuencias dibujadas y capturas de Red Magic/POCO ejecutadas en emulador son
pruebas de imágenes, no pruebas físicas. No hay una medición nueva en teléfono
que permita prometer una latencia concreta. Las recuperaciones difíciles aún
pueden esperar la pasada completa periódica; no se afirma latencia uniforme.
No se cambia main ni se promueve esta corrección antes de aprobar fix-error.
