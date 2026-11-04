# Coach 1.1.10.472 — build 1188

## Auditoría portuguesa aislada

- Archivos modificados: `.github/workflows/build-apk.yml`, `.github/scripts/test_parallel_validation.py`, `tools/audit-portuguese-device.py`, `tools/test_portuguese_device_audit.py`, `app/build.gradle.kts` y este registro.
- El único fallo de la validación completa provenía de un aviso recibido en español. Su mensaje es contenido redactado por el emisor, por lo que no debe confundirse con una cadena de la interfaz; los títulos y controles del aviso siguen auditándose.
- El flujo completo admite una ejecución dirigida del único trabajo rojo y descarga los APK ya construidos en la ejecución anterior. Los trabajos verdes no vuelven a ejecutarse en ese modo.
- La ejecución dirigida comprueba el APK previo y el script actualizado; no representa una compilación final del nuevo número de versión.

## Verificación

- Prueba unitaria de separación entre texto del aviso e interfaz.
- Validación sintáctica del flujo y recursos localizados.
