# Coach 1.1.10.492 — Auditoría portuguesa con selección de idioma resistente

## Diagnóstico

- La última validación falló en `installed-audit` antes de probar las pantallas
  de la aplicación.
- El emulador publicó la fila de idioma, pero perdió el primer toque: el botón
  seguía mostrando `Continuar en Español` cuando la auditoría esperaba el flujo
  portugués.
- La compilación del APK, la auditoría española y las pruebas funcionales
  restantes terminaron correctamente.

## Corrección

- La auditoría portuguesa reintenta la selección del idioma hasta cuatro veces
  y exige comprobar el texto `Continuar em Português` antes de avanzar.
- Si el estado no cambia después de los reintentos, conserva la captura y falla
  con un diagnóstico explícito; no oculta un problema real del APK.
- Se añadió una prueba unitaria que simula el primer toque perdido y verifica
  que el segundo intento solo continúa cuando aparece el estado portugués.
- Se incrementó la versión de producción a `1.1.10.492`.

## Verificación

- `python3 -m py_compile` para la auditoría y sus pruebas: correcto.
- `python3 -m unittest tools.test_portuguese_device_audit -v`: 10 pruebas
  correctas.
- `git diff --check`: correcto.
- La auditoría instalada completa se confirmará en GitHub Actions con el APK
  firmado del flujo.

## Alcance de operación

No se añadió monitoreo continuo a la aplicación. El reintento existe únicamente
en la comprobación automatizada del emulador y conserva evidencia ante un fallo
real.
