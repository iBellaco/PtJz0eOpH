# Seguimiento de recomendaciones — 01/11/2026

Alcance autorizado: los dos informes. IDs A corresponden al PDF de aplicación;
IDs S, al de ciberseguridad. «Implementado» no significa validado en Android.
No cerrar puntos sin sus criterios de aceptación. La entrega 442 es un bloque,
no la finalización de las 54 recomendaciones.

| ID | Tema | Estado / siguiente evidencia necesaria |
|---|---|---|
| A01 | Energía adicional | Pendiente: mediciones físicas controladas |
| A02 | Consultas durante juego | Implementación 442; medir peticiones y actualización manual |
| A03 | Cierre de burbuja | Existente 441; pendientes 20 gestos por dispositivo |
| A04 | Actualizaciones ordinarias | Implementación 442; pruebas Android pendientes |
| A05 | Restauración recuperable | Parcial 442: historial transaccional; falta recuperación conjunta con perfiles |
| A06 | Validación de respaldos | Implementación 442; pruebas Android pendientes |
| A07 | Reanudación de captura | Existente 441; pendientes ciclos en teléfonos |
| A08 | Temperatura | Pendiente: política térmica y medición |
| A09 | Permisos recuperables | Existente parcial; matriz API 34–36 pendiente |
| A10 | Últimas selecciones | Existente parcial; mediciones por fase pendientes |
| A11 | Confirmación visible | Pendiente de revisión de interfaz |
| A12 | Correcciones manuales | Existente; verificar secuencias contradictorias |
| A13 | Indicadores del buzón | Pendiente: coherencia multidispositivo |
| A14 | Vacío/carga/error | Pendiente: recorrido completo |
| A15 | Modo sin conexión | Pendiente: contrato funcional y pruebas |
| A16 | Registro por etapas | Existente parcial; completar recuperación |
| A17 | Diagnóstico voluntario | Parcial 442; falta vista previa/ocultación/retención |
| A18 | Imágenes sin tirones | Parcial 442: buzón; faltan otros visores y medición |
| A19 | Antigüedad del contenido | Pendiente: separar etiquetas de conexión y contenido |
| A20 | Coherencia del catálogo | Comprobaciones existentes; ampliar casos inválidos |
| A21 | Categorías por parche | Existente; verificación multidispositivo pendiente |
| A22 | Identidad de sesión | Pendiente: migración de historial y reintentos |
| A23 | Datos históricos desconocidos | Pendiente: representación y edición sin inferir |
| A24 | Estimaciones | Pendiente: ausencia de datos diferenciada de 50 % |
| A25 | Accesibilidad y espacios | Pendiente: revisión visual y texto ampliado |
| A26 | Filtros y posición | Pendiente: persistencia y recreación |
| A27 | Idiomas | Nuevos recursos comprobados; falta recorrido integral |
| A28 | QR local | Pendiente: generador y lectura independiente |
| A29 | Recomposiciones | Pendiente: perfilado antes de optimizar |
| A30 | Reporte voluntario | Pendiente: interfaz de revisión y envío |
| S01 | Capacidades web | Implementación 442; reproducción y pruebas Android pendientes |
| S02 | Dominio real | Implementación 442; pruebas Kotlin pendientes |
| S03 | Navegación web | Parcial 442; revisar flujos externos y permisos |
| S04 | Archivos compartibles | Implementación 442; verificar concesiones en dispositivo |
| S05 | Capturas de diagnóstico | Parcial 442: nuevo consentimiento; falta caducidad |
| S06 | Minimizar captura/envío | Pendiente: controles de revisión |
| S07 | Autorización remota | Pendiente: evidencia efectiva del servicio, no inferir desde cliente |
| S08 | Revocación y sesión | Pendiente: aislamiento y pruebas cruzadas |
| S09 | Integridad exigida | Pendiente: configuración remota y canales legítimos |
| S10 | Fallo del cifrado | Pendiente: separar preferencias no sensibles de almacén seguro |
| S11 | Clasificación local | Pendiente: inventario y política de almacenamiento |
| S12 | Copias mínimas | Parcial: importación limitada; revisar datos exportados y recuperación |
| S13 | Contraseña coherente | Política local existente; servidor pendiente de verificar |
| S14 | Intentos y recuperación | Pendiente: control remoto y sesiones recientes |
| S15 | Capturas selectivas | Pendiente: proteger pantallas sensibles sin bloquear juego |
| S16 | Entradas externas | Pendiente: validar intents y acciones exportadas |
| S17 | Importación/medios | Parcial 442; completar recuperación e imágenes en otros visores |
| S18 | Excepciones | Pendiente: sustituir recuperación global amplia por manejo específico |
| S19 | Registros | Parcial 442: registradores centrales; revisar usos directos y expiración |
| S20 | Transporte | Prohibición de tráfico claro existente; pruebas integrales pendientes |
| S21 | Actualizaciones auténticas | Firma persistente existente; no hay instalador integrado |
| S22 | Señales de manipulación | Existente sin bloqueo indiscriminado; verificar límites |
| S23 | Dependencias/release | Pendiente: análisis de dependencias y APK final |
| S24 | Matriz de seguridad | Parcial 442: pruebas añadidas; completar cobertura y dispositivos |

## Orden de continuación

1. Compilar y ejecutar las pruebas Android de 442; corregir cualquier regresión antes
   de acumular más cambios. El entorno local no puede descargar Gradle.
2. Completar A05/S12 y A17/S05, después sesiones/autorización y registro.
3. Reconocimiento, historial, contenido y accesibilidad.
4. Medición física y comprobación remota efectiva: no sustituirlas por pruebas estáticas.

Mantener una sola promoción a validación para el conjunto revisado y crear su
solicitud hacia main cuando corresponda. No cancelar ejecuciones activas.
