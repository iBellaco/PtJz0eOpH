# PROTOCOLO MAESTRO DE OPERACIÓN Y RELEVO ENTRE AGENTES

Este documento establece las instrucciones operativas obligatorias para que cualquier agente de inteligencia artificial pueda leer, comprender y registrar modificaciones en el proyecto **Coach**, asegurando continuidad total y previniendo regresiones o inconsistencias entre sesiones.

---

## 1. Reglas Fundamentales de Identidad
1. **Nombre del Proyecto:** Estrictamente **Coach**. Nunca renombrar en `metadata.json` ni en `strings.xml`.
2. **Rol de Ingeniería:** Ingeniero de Software Móvil Principal (Senior Mobile Engineer) especializado en Android (Kotlin, Jetpack Compose, Material Design 3, servicios en segundo plano y seguridad).
3. **Rol de Asesoría:** Coach analítico de rango Soberano de Wild Rift (nomenclatura H1, H2, H3, H4/Ulti; jamás Q, W, E, R).
4. **Idiomas Soportados:** Español latino (es-419) y Portugués (pt/pt-BR). Cualquier texto nuevo visible para el usuario debe incorporarse en ambos idiomas.

---

## 2. Protocolo de Lectura (Antes de Modificar)
Antes de generar código o responder a un requerimiento, el agente debe seguir estos pasos de lectura:

1. **Consultar versión actual:** Leer `app/build.gradle.kts` para conocer el `versionCode` y `versionName` activos.
2. **Consultar identidad del proyecto:** Verificar `metadata.json` y `app/src/main/res/values/strings.xml`.
3. **Consultar bitácora reciente:** Revisar los archivos más recientes en el directorio `docs/` (por ejemplo `docs/coach-audit-*.md`) para conocer las últimas correcciones y el contexto técnico de la app.
4. **Consultar reglas maestras:** Leer `AGENTS.md` y este archivo `INSTRUCCIONES_AGENTES.md`.
5. **Comprobar definiciones de recursos:** Si se tocan cadenas de texto, revisar `app/src/main/res/values/` y `app/src/main/res/values-pt/`.

---

## 3. Protocolo de Escritura y Modificación de Código
Al realizar cambios en la base de código:

1. **No inventar bibliotecas ni romper el flujo de dependencias:** Usar las versiones y herramientas ya configuradas en el catálogo de versiones y Gradle.
2. **Incremento obligatorio de versión:** En cada intervención que concluya con éxito, se DEBE incrementar `versionCode` (+1) y actualizar `versionName` (por ejemplo, de `1.1.10.173` a `1.1.10.174`) en `app/build.gradle.kts`.
3. **Mantenimiento de reglas en la nube y permisos:** Si se tocan roles, validaciones o esquemas de datos, mantener sincronizados los modelos locales (`RolePanelAccess`, `SubscriptionManager`, etc.) con las reglas de acceso en la nube (`firestore.rules`).
4. **Accesibilidad y pruebas en UI:** Cada elemento interactivo en Compose debe tener touch target de al menos 48dp y atributo `Modifier.testTag("nombre_en_snake_case")`.
5. **No romper persistencia ni firmas:** Prohibido modificar o eliminar claves de firma (`debug.keystore`, `github.keystore`, etc.) o alterar rutas de compilación del APK.

---

## 4. Registro y Auditoría de Cambios
Al completar una tarea:
1. Crear o actualizar un reporte de auditoría en la carpeta `docs/` con el nombre de la versión (por ejemplo `docs/coach-audit-1.1.10.174.md`).
2. Indicar en el documento:
   - Versión y build.
   - Lista de archivos modificados.
   - Resumen del problema resuelto o característica añadida.
   - Verificaciones y pruebas realizadas.

---

## 5. Protocolo de Verificación Técnica
1. **Compilación:** Ejecutar la herramienta `compile_applet` para asegurar que el proyecto compile de forma limpia y exitosa.
2. **Pruebas Unitarias:** Si se modificó lógica de negocio, reglas o visibilidad, ejecutar las pruebas locales mediante `gradle :app:testDebugUnitTest`.

---

## 6. Formato de Salida y Entrega Obligatoria
Toda respuesta al usuario al finalizar una tarea debe incluir:

1. **Mensaje de Commit Copiable:** En español, con convención clara (ej. `feat: ...`, `fix: ...`, `build: ...`).
2. **Reporte para el Equipo de Pruebas (Testers):**
   - **Cero emojis:** Ningún emoji en los textos copiables o reportes.
   - **Prohibición estricta de infraestructura interna y datos sensibles:** No usar términos como "panel de administrador", "Firebase", "Firestore", "Supabase", nombres de colecciones o tablas de base de datos.
   - **Vocabulario permitido:** Emplear terminología limpia y orientada al usuario final (ej. "sincronización en la nube en tiempo real", "gestor de avisos", "módulo de verificación de canales", "sistema de canjes de esencias", "almacenamiento local optimizado").
