# Auditoría Coach 1.1.10.256

- Build: 972.
- Baneos: lectura y presentación para todas las cuentas, con cinco posiciones por equipo. Todos los círculos del visor usan el mismo radio mediano; los nombres confirmados se dibujan inmediatamente debajo y las posiciones desconocidas conservan «Ban». La etiqueta del primer slot pasa debajo de su avatar durante la lectura de baneos para no ocupar el mismo espacio que los nombres superiores.
- Reconocimiento: búsqueda local de escala y posición para retratos superiores sin identificar, conservando el umbral de confianza, la separación entre candidatos y la confirmación en dos lecturas. La búsqueda inicial y los retratos ya identificados conservan su recorrido.
- Calibración: el visor del hub ofrece los diez avatares individuales además de las dos columnas. Las cuatro direcciones y el tamaño modifican únicamente el objetivo elegido y persisten mediante la configuración existente.
- OCR: franjas cortas pegadas al borde de cada avatar; el visor muestra exactamente el rectángulo que analiza el lector.
- Pruebas añadidas: botones reales de los diez objetivos; captura Red Magic para el retrato antes desconocido y radio uniforme; primera selección Seraphine/Mel mediante el lector Android nativo con las coordenadas Red Magic y Poco proporcionadas.
- Verificación requerida antes de publicar: integridad de catálogos, portugués, lint, suite Android, compilación release R8, certificado persistente y APK instalado en español y portugués.
- Archivos: configuración y visor de calibración, clasificador y seguimiento de baneos, visor de zonas de lectura, integración del hub, traducciones portuguesas, pruebas, versión y esta auditoría.
- Límite: las coordenadas se validan con las dimensiones de la captura y pruebas automatizadas; no se afirma haber probado físicamente ambos dispositivos.
