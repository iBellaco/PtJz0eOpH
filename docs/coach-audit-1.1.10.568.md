# Coach 1.1.10.568 — corrección del test de hechizo de jungla y triggers de validación

## Correcciones implementadas

- Se corrigió la expectativa de la prueba de builds de jungla para comparar con
  el nombre canónico del catálogo. `Castigo` es el alias de entrada y
  `Aplastar` es la etiqueta que Coach muestra en español; la regla sigue
  colocando ese hechizo en el primer espacio y conserva un segundo hechizo
  distinto.
- La búsqueda de una APK completamente validada también se habilitó en pushes
  directos a `coach-validacion`. Cuando existe una ejecución completa exitosa
  con las mismas fuentes, los jobs reutilizan su artefacto firmado mediante
  `actions/download-artifact`; si no existe, cada job conserva su ejecución
  independiente y sus artefactos verdes quedan disponibles para los jobs
  posteriores del mismo run.
- Se añadieron triggers de pull request para `coach-validacion` y `fix-error`
  sin cambiar la separación entre la validación completa y la selectiva.

## Versión

- `versionCode`: 1284
- `versionName`: `1.1.10.568`

## Causa del fallo diagnosticado

El job `core-tests` falló únicamente en
`ChampionRoleAdapterBuildRulesTest.jungle build always places smite before the secondary spell`.
El test esperaba `Castigo`, pero `WildRiftSpellsAndRunes` resolvía el alias al
nombre canónico `Aplastar`. Los demás jobs y artefactos de la ejecución
terminaron correctamente.

## Verificaciones

- `git diff --check`: completada.
- Sintaxis YAML de los seis workflows: parseada correctamente con PyYAML.
- `python3 -m unittest discover -s .github/scripts -p test_validated_apk.py`: 16 pruebas correctas.
- Suite Android: se ejecutará en GitHub Actions; el entorno local no tiene la
  distribución Gradle 9.3.1 y su descarga fue rechazada.
