# Coach 1.1.10.356 — build 1072

La ejecución aislada `fix-error` del commit `e67b5fad` no llegó a probar los
dos fallos Red Magic: el runner interpretó el heredoc Python como comandos de
shell y terminó con `/usr/bin/sh: import: not found`. El escáner no fue
ejecutado en ese job.

Se reemplazó el heredoc por `tools/verify-targeted-red-magic.py`, que instala
el APK release ofuscado y ejecuta solo las dos regresiones fallidas. Se añadió
además `ChampionNameResolverCoverageTest`, que comprueba los 142 campeones
del catálogo mediante su título completo, sin abreviaturas ni coincidencias de
subcadenas. Esta prueba acredita cobertura de nombres del catálogo, no prueba
físicamente 142 campeones en cada teléfono.

La próxima ejecución de `fix-error` debe aprobar primero el diagnóstico Red
Magic. La validación completa no se trasladará hasta conocer el resultado.
