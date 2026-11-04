# Coach 1.1.10.474 — build 1190

## Entrega de prueba instalable

- Archivos modificados: `app/build.gradle.kts`, `.github/workflows/build-apk.yml`, `.github/scripts/test_parallel_validation.py` y este registro.
- Problema: el APK `1.1.10.473-prueba.1` se generó con una firma temporal diferente de la firma persistente usada para actualizar Coach. Android rechaza una actualización con identidad de firma distinta.
- En `main` y `coach-validacion`, las compilaciones ahora exigen la firma persistente incluso en modo de prueba; las pruebas de otras ramas pueden seguir usando una firma temporal sin publicar.
- La entrega de prueba en `main` compila un APK nuevo, comprueba su firma, versión, procedencia y ofuscación, y publica únicamente el APK directo y el ZIP del proyecto en un prelanzamiento identificado como prueba. No se marca como actualización oficial.
- `versionCode` sube a 1190 para permitir actualizar desde el APK anterior con código 1189 cuando la firma instalada coincida con la identidad persistente.

## Verificación

- Prueba estructural del flujo y política de reutilización de APK.
- Validación completa e instalación quedan pendientes de la ejecución de Actions con la clave privada, que no está disponible en este entorno local.
