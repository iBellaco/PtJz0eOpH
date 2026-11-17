# Coach 1.1.10.569 — sincronización de la prueba de información en portugués

## Corrección implementada

- Se actualizó `LocalizationSurfaceTest` para reflejar la regla vigente de la
  pantalla Información: el número de compilación y la etiqueta de compilación
  no aparecen allí; la versión con build permanece únicamente en la pantalla
  principal.

## Causa raíz del run fallido

El job `core-tests` falló en
`LocalizationSurfaceTest.initial Portuguese selection opens the complete information panel in Portuguese`.
La aserción esperaba dos nodos con `Compilação`, pero la interfaz correctamente
ya no renderiza ese texto en Información. `build-apk`, los escáneres y las
auditorías instaladas terminaron correctamente; `build-and-release` quedó rojo
solo por `CORE_RESULT=failure`.

## Artefactos y flujo

- Se mantiene la separación existente: las suites independientes conservan sus
  artefactos y los jobs posteriores los descargan con `actions/download-artifact`.
- La reutilización de una APK completa solo se permite cuando la ejecución
  anterior terminó completamente verde y las fuentes de APK son idénticas. La
  ejecución fallida no se reutiliza como validación completa.

## Versión

- `versionCode`: 1285
- `versionName`: `1.1.10.569`

## Verificaciones

- `git diff --check`: completada.
- Sintaxis YAML de los seis workflows: parseada correctamente con PyYAML.
- `test_validated_apk.py`: 16 pruebas correctas.
- `test_parallel_validation.py`: 15 pruebas correctas.
- Suite Android: se validará en GitHub Actions; el entorno local no dispone de
  la distribución Gradle 9.3.1.
