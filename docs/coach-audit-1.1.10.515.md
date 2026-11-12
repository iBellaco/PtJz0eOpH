# Auditoría de Coach 1.1.10.515

## Cambios

- Se limita el envío explícito de diagnósticos del hub a cinco usos por día y por dispositivo. El contador usa la fecha local, conserva los usos restantes visibles para accesibilidad y devuelve el cupo si el envío falla.
- El icono de diagnóstico sigue disponible con el autoescáner pausado: así puede informar precisamente que la captura no está activa, sin mantener muestreo en segundo plano.
- El diagnóstico conserva la medición puntual de batería de la sesión (porcentaje, mAh/Wh observados, corriente, voltaje, potencia, temperatura y salud), además de FPS, frames, tiempos de procesamiento, memoria y estado térmico.

## Verificación

- `:app:compileDebugKotlin` y `:app:testDebugUnitTest --tests com.example.DeviceDiagnosticQuotaTest`.
- El límite es local y no sustituye la autorización del panel de soporte; no se envía ningún dato automáticamente.
- La firma de producción continúa dependiendo del flujo protegido con el keystore de producción.
