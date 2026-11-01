# Coach 1.1.10.430 — build 1146

## Cambios
- FloatingAssistantService: cada ventana Compose recibe propietarios propios de ciclo de vida, estado guardado y ViewModelStore; restaura antes de componer y libera al destruir. La ausencia del propietario de estado guardado podía lanzar una excepción al adjuntar la ventana.
- AuthViewModel: el registro deja la creación del perfil para el primer acceso verificado. El intento anterior escribía después de enviar el correo, pero las reglas exigen correo verificado; su rechazo se mostraba incorrectamente como fallo de creación de cuenta.
- Un fallo posterior a crear la identidad informa que la cuenta existe y permite continuar desde inicio de sesión. Las pulsaciones duplicadas se bloquean durante el registro.
- PasswordPolicy: exige longitud mínima 8, mayúscula, minúscula, número y símbolo; espacios no cuentan como símbolo. El indicador Fuerte usa exactamente esa política.
- find-validated-apk.py: admite validación por push y ejecución manual en coach-validacion, conservando identidad del repositorio, comparación de archivos, todos los jobs requeridos, artefacto vigente y comprobación posterior de firma.
- Cadenas nuevas en español, portugués y portugués brasileño; pruebas específicas añadidas al flujo fix existente.

## Verificación
- Pruebas de reutilización y auditoría de recursos localizados ejecutadas localmente.
- Auditoría completa de portugués: cero residuos españoles en los textos visibles. Política de contraseña compilada con Kotlin y ejecutada con JUnit fuera de Android.
- Pruebas JVM añadidas: requisitos de contraseña y montaje real de Compose con rememberSaveable y destrucción de la composición.
- Gradle local bloqueado al descargar su distribución (Connection refused); compilación, pruebas Android y APK ofuscado pendientes de Actions. No se afirma validación física ni se entrega un APK anterior como si incluyera estos cambios.
- No se modificaron datos ni políticas remotas de cuentas. La política de contraseña aquí comprobada corresponde al formulario de registro de Coach.
