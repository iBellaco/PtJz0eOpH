# Auditoría Coach 1.1.10.255

- Build: 971.
- Problema: el contenido de registro excedía el alto disponible y el contenedor no ofrecía desplazamiento; «Crear perfil» y el enlace de inicio de sesión podían quedar recortados.
- Corrección: contenedor común de autenticación con un alto máximo definido por el espacio del padre, desplazamiento vertical y ajuste a los márgenes del teclado. En padres sin límite de alto se usa el alto de pantalla para conservar un área desplazable finita. Al cambiar de formulario se vuelve al inicio.
- Alcance: registro, inicio de sesión y recuperación de contraseña comparten la solución. Se conservan textos, validación, creación de cuentas y firma. El botón de registro mantiene 52 dp y recibe una etiqueta de prueba propia.
- Compatibilidad del teclado: la actividad declara `adjustResize`, necesario para entregar los márgenes del teclado a Compose también en Android anteriores a API 30.
- Archivos: `AuthComponents.kt`, `AuthScreen.kt`, `RegisterScreen.kt`, `AuthFormViewportTest.kt`, `AndroidManifest.xml`, `app/build.gradle.kts` y esta auditoría.
- Verificación: prueba del formulario real en español y portugués, 320 dp de ancho, texto al 130 %, desplazamiento por gesto y acceso a las acciones después de reducir el alto de 420 a 180 dp. Integridad, revisión portuguesa, suite Android, compilación R8, firma e instalación en ambos idiomas obligatorias antes de integrar.
- Límite: reducir el alto en la prueba reproduce el espacio disponible; las comprobaciones de la entrega instalada se ejecutan adicionalmente en emuladores. No se afirma haber probado físicamente todos los modelos.
