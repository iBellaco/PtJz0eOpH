# Coach 1.1.10.140 — información inicial en portugués

- El panel de información, la presentación inicial y las tres pestañas de información legal usan 103 textos completos incluidos en recursos Android para español y portugués.
- La elección explícita del idioma determina qué recursos se muestran desde el primer renderizado. Cambiar el idioma actualiza los textos sin depender del idioma del teléfono ni de una traducción por fragmentos.
- Los componentes de estas pantallas reciben textos ya localizados y los muestran sin volver a traducirlos. El párrafo del parche y las etiquetas de versión usan formatos completos con parámetros.
- Se amplía LocalizationSurfaceTest con el recorrido real de selección inicial de portugués, desplazamiento por el panel, cambios español/portugués y navegación por las tres pestañas legales.
- Verificación local: XML válido, 103 pares completos, mismos parámetros de formato en ambos idiomas, referencias resueltas y búsqueda de residuos españoles. Las pruebas de interfaz y el APK se validan mediante GitHub Actions.

## Reporte para testers

Coach 1.1.10.140 (856): revisar la selección inicial de portugués, todas las secciones de Información, las tres pestañas legales y la presentación inicial. Comprobar también el cambio a español y el regreso a portugués sin reiniciar.

## Mensaje de commit

Corrige el portugués de la información inicial y valida el cambio de idioma
