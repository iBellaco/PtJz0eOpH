# Auditoría de Coach — 1.1.10.220 (build 936)

## Alcance y estado del registro

Registro de cambios y comprobaciones de esta revisión. Se redacta antes de la validación de entrega en Actions; el resultado de esa validación debe consultarse en las ejecuciones de esta versión y en el acta final descargable que acompaña al APK. Esta descripción de cuándo se redactó el registro no indica que una compilación posterior esté pendiente o haya fallado.

## Identidad y Secret comprobados

- El APK publicado 219 se descargó de la release v1.1.10.219-b935. Su manifiesto declara com.Coach, versión 1.1.10.219, código 935, y su SHA-256 coincide con la entrega: b4f39c0287647c65f13dbc08ef54dfcda326a72ad8f09710f3839c9b2ea939b1.
- com.example es el namespace y paquete del código; no es el applicationId instalado. Se conserva com.Coach y la identidad de firma para admitir actualizaciones de la aplicación existente. CN=Coach, O=Coach, C=US; certificado SHA-256 27dba5165e26d0da19274edfd21a8f0439e6d7d7cc979efd449c7c71c7b4c055.
- El identificador indicado por el propietario, com.aistudio.wildriftdrafting.wrdftx, figura como segundo cliente Android en el JSON privado, junto a com.Coach. Se comprobaron únicamente sus nombres, sin publicar claves. La auditoría histórica 178 ya mencionaba ese registro: su expresión “compatibilidad dual” no convierte ambos paquetes en alias instalables. No se acredita qué ficha esté actualmente activa en Google Play; para actualizar el APK publicado corresponde com.Coach y su certificado existente. No se cambia applicationId sin una decisión explícita sobre una aplicación distinta.
- El propietario confirma que creó COACH_GOOGLE_SERVICES_JSON. El run 37703548670 completó la restauración del Secret en los dos trabajos consumidores; el run 37706124268 publicó la entrega validada. El HTTP 403 previo era un rechazo a crear el Secret desde esta conexión. No es un fallo actual de compilación y no se atribuye a esta conexión la creación realizada por el propietario.
- Se corrige el estado obsoleto de la auditoría 219 en el repositorio y se documenta la distinción de identificadores en el README.

## Hallazgos y correcciones

- “Avatar exclusivo” y “Temas exclusivos” son expresiones válidas también en portugués. No se presentan como errores de idioma. Se revisan adicionalmente los títulos, regiones, rarezas y descripciones reales del catálogo de avatares y temas, y los cambios de idioma con las pantallas abiertas.
- Los avatares bloqueados no podían abrir el aviso Premium que ya estaba definido. Ahora una pulsación abre ese aviso sin cambiar ni desbloquear el avatar. Se impiden pulsaciones durante una actualización en curso.
- Se añade la traducción del título de avatar “La Asesina Sigilosa”. Se verifica el texto dinámico del avatar bloqueado y su título/región.
- La navegación y el encabezado de clasificación dejan de mostrar “Tier List”. Se localizan en ES/PT la vista previa de Coach y otras etiquetas de temas, incluido “Nivel Soberano”. No se ofrece inglés como idioma seleccionable. Se mantiene la migración de una preferencia antigua no admitida a español.
- Se retiran nueve claves de traducción inactivas de la antigua región NA y su alias inverso. La palabra portuguesa “na” es una contracción legítima; no se elimina de frases portuguesas. La región activa continúa siendo global.
- Se corrige el recurso pt-BR del historial: 48 horas, igual que ES y PT, en lugar de siete días.
- Se corrige la introducción que prometía una clasificación “oficial”, actualizada al último parche y servidores asiáticos. Ahora identifica fuentes comunitarias globales y explica que pueden estar desactualizadas y no garantizan resultados.
- Los temas dejan de presentarse como oficiales de Riot. El aviso Premium aclara que son temas de Coach inspirados en Runaterra y que no acreditan aprobación de Riot. El botón Aplicar tiene un área mínima de 48dp y un identificador de prueba.
- La comprobación visual inicial encontró que la pantalla de temas no permitía desplazarse hasta todo el contenido. Se agrega desplazamiento vertical. Aplicar mostraba la insignia Premium pero permitía cambiar a otro tema sin esa condición; ahora abre el aviso sin cambiar el tema. La insignia también tiene un área de 48dp. Las dos nuevas pruebas de avisos fallaron inicialmente por el selector del campo de búsqueda y por la ausencia de desplazamiento; se corrigen las interacciones y la pantalla antes de validar la entrega.
- La captura de la vista previa mostró “Aplicar” partido en dos líneas junto a su insignia. Se coloca la fila de acciones debajo del texto de ejemplo y se reserva el ancho disponible para el botón, manteniendo el texto en una línea.
- La primera revisión instalada completa aprobó ES y detectó “Cerrar hoja” en la accesibilidad de temas en PT: la actividad retenía recursos españoles mientras los textos propios ya usaban portugués. La selección de idioma actualiza ahora también los recursos del contexto que llamó a la selección, además de los de la aplicación. Se añade una regresión con un contexto de configuración independiente. No se publica como válida la ejecución 37709533890, que falló esta comprobación; se exige repetirla con el APK corregido.

## Comprobaciones

- Treinta pruebas Python de scripts de compilación y cinco del auditor de texto del dispositivo aprobadas.
- Pruebas locales de núcleo: 142 pruebas en 21 clases, sin fallos, errores ni omisiones; incluyen los nuevos casos de catálogos, cambio de idioma y recursos regionales.
- Después del fallo instalado de idioma, se ejecutó y aprobó LocalizationRegressionTest con la regresión adicional del contexto que cambia de ES a PT y vuelve a ES. El conjunto completo se vuelve a ejecutar sobre esta corrección en la validación de entrega.
- Cuatro casos visuales locales de avatares, temas y sus avisos Premium aprobados después de las correcciones. Usan una sesión local sin Premium, comprueban controles visibles y sus acciones de accesibilidad, y verifican que abrir el aviso no cambie el avatar ni el tema. El recorrido instalado comprueba los toques de temas en Android.
- Generador de builds en modo --check: 142 campeones, 300 perfiles de línea, sin diferencias de generación.
- Auditoría estática: 2536 apariciones de literales visibles, sin hallazgos del patrón de español usado por la herramienta. Ese patrón no demuestra corrección lingüística de toda la aplicación; se amplía la comprobación con catálogos y pantallas reales.
- Regresiones añadidas: campos reales de todos los avatares/temas, recursos PT/PT-BR/ES del historial, cambio de idioma sin reabrir ambos catálogos y renderizado de los avisos Premium.
- El recorrido del APK instalado amplía su revisión con temas, vista previa y aviso Premium en ES y PT. El catálogo de avatares y su aviso se comprueban con composables reales y datos locales; no se crea ni modifica una cuenta real para hacerlo.
- Los flujos conservan las comprobaciones anteriores de OCR, décima selección, nombres de relaciones, límites 3/6/12, consejos de objetos/runas/botas/hechizos, ausencia de consejo para Aplastar y eliminación de cuenta con doble confirmación. No se declara que hayan pasado en esta entrega antes de obtener sus resultados.
- compile_applet no está disponible en esta sesión. La comprobación de compilación utiliza Gradle y Android SDK; la validación final exige la variante release firmada, ofuscación real por R8 y ejecución instalada en Android.

## Límites y asuntos que siguen abiertos

- No se ha acreditado una autorización de Riot para Coach ni una aceptación de Google Play. Los textos informativos y el certificado Android no conceden esas autorizaciones. El uso y la monetización de recursos de Riot, así como la asistencia durante partidas, siguen requiriendo revisión y autorización aplicable; esta intervención no certifica su cumplimiento.
- La política de recuperación durante 60 días y sus dos confirmaciones se mantienen. El plazo es una decisión del producto, no una aprobación de Google ni una garantía jurídica universal.
- Quitar valores del workflow no los elimina de commits históricos ni del APK cliente. No se reescribe el historial, se rota una clave ni se modifica la firma.
- Solo se certifican las comprobaciones efectivamente ejecutadas y las superficies examinadas; no se garantiza traducción perfecta de todos los textos, precisión total del OCR ni exactitud externa del catálogo.

## Archivos afectados

README.md, metadata.json, app/build.gradle.kts, traducciones ES/PT, recursos strings.xml ES/PT/PT-BR, startup_information.xml ES/PT, AvatarSelectionDialog.kt, ThemeCustomizationDialog.kt, AppLanguage.kt, LocalizationRegressionTest.kt, LocalizationSurfaceTest.kt, PortugueseRenderedAuditTest.kt, tools/audit-portuguese-device.py y las auditorías 219/220.
