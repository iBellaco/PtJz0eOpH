# Coach 1.1.10.559 — reutilización determinista del APK validado

Versión: 1.1.10.559; versionCode: 1275.

## Corrección

- El detector de entregas busca las validaciones únicamente en
  `coach-validacion`, con una página amplia de ejecuciones completadas, en vez
  de depender de la primera página mezclada de todas las ramas.
- Las consultas a la API de acciones tienen reintentos breves para absorber la
  consistencia eventual de ejecuciones y artefactos recién terminados.
- Se añadió una regresión que comprueba la rama, el estado y el tamaño de la
  búsqueda.

## Resultado esperado

Cuando `main` recibe el mismo árbol que una validación verde, reutiliza el APK
validado y omite las suites pesadas de compilación, escáner y pruebas Android.
Solo conserva las comprobaciones de publicación necesarias.

## Validación local

- `python3 -m unittest discover -s .github/scripts -p test_validated_apk.py`: 16
  pruebas correctas.
- `python3 -m py_compile` de los scripts modificados: correcto.
