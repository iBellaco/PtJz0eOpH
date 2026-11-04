# Coach 1.1.10.473 — build 1189

## Validación completa de una versión de prueba

- Archivos modificados: `.github/workflows/build-apk.yml`, `.github/scripts/test_parallel_validation.py`, `.github/scripts/package-delivery.py`, `app/build.gradle.kts` y este registro.
- La ejecución completa puede solicitarse con `test_build=true` desde `main` sin crear una publicación oficial.
- Esa ejecución compila e instala `1.1.10.473-prueba.1`, ejecuta todas las comprobaciones y conserva el APK con nombre de prueba junto con un ZIP del código sin archivos privados.
- La ruta ordinaria de producción sigue exigiendo firma persistente y el formato numérico.

## Verificación

- Validación de sintaxis y condiciones del flujo.
- Pruebas locales de la estructura de validación.
