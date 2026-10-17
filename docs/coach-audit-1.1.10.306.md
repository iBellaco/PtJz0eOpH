# Coach 1.1.10.306 — build 1022

## Cambios verificados

- El selector de línea de cada campeón conserva la línea principal y solo muestra líneas Flex cuando la sincronización contiene al menos una tasa publicada para esa combinación campeón/línea. Se evita presentar una ficha Flex con todos los valores vacíos.
- La ficha sigue leyendo el registro específico de la línea elegida; no reutiliza porcentajes globales ni los de la línea principal para completar una Flex.
- Android usa el selector estándar de captura. En Android 14 o posterior permite autorizar únicamente Wild Rift o toda la pantalla, según la opción elegida por el usuario.
- Se actualizaron las cuatro páginas informativas iniciales en español y portugués para describir el análisis del draft, el escáner, la clasificación por fuentes públicas y las funciones actuales de la suscripción.
- Ventaja, Débil y Sinergia anuncian 24 resultados Premium. Para visitantes, el acceso a registro ahora es una llamada visual animada que abre directamente el formulario de creación de cuenta.
- Las insignias de funciones bloqueadas se ocultan cuando la suscripción ya está activa, incluidos Auto-Scan y la variante Flex.
- Los consejos de Malla de espinas, Medallón de los Solari, Flechas de Yun Tal y Puñal de Statikk describen su estado actual sin frases históricas de subida o bajada de precio.

## Validación

- Prueba unitaria añadida para confirmar que una línea Flex sin tasas publicadas queda fuera del selector y que una línea con datos se conserva.
- Revisión estática de recursos en español y portugués, navegación a registro, flujo de captura y etiquetas por estado de suscripción.
- `git diff --check` sin errores.
- La ejecución local de Gradle requirió descargar la distribución y después quedó bloqueada porque este entorno no incluye Android SDK. La compilación completa queda a cargo de los controles automatizados del repositorio, que sí proporcionan el SDK.

## Versión

- `versionCode`: 1022
- `versionName`: 1.1.10.306

## Corrección posterior de catálogo

- Se eliminaron referencias al precio anterior o a aumentos y reducciones de oro en el consejo táctico de objetos.
- Se regeneraron los 300 perfiles de línea en español y portugués con criterios de uso actuales.
- Se corrigió la traducción portuguesa de Flechas de los Yun Tal detectada por la suite completa.

- Se añadieron traducciones portuguesas completas para los cuatro consejos de objetos actuales validados por la suite renderizada.
