# Auditoría Coach 1.1.10.260

- Build: 976.
- Perfil Poco X7 Pro: coordenadas medidas para modelos comercial/global/India; instalación nueva y migración del preset genérico intacto. Conserva ajustes individuales existentes y baneos manuales. Restablecer utiliza el perfil del teléfono.
- Visor: las etiquetas del primer slot vuelven al espacio superior del avatar; evita ocupar el título del segundo slot y taparlo en el OCR. El círculo del décimo utiliza la coordenada individual del avatar final.
- Décimo pick: nueve títulos confirmados identifican el lado restante y corrigen un prefijo previo errado; al llegar a nueve títulos, el último avatar aliado se puede reconocer aunque aún muestre el nombre de la línea. Antes de nueve no se confirma por esa vía. Mantiene la elección manual explícita.
- Baneos: amplía la búsqueda local de posición/tamaño en retratos desconocidos conservando el umbral de confianza y separación. Regresión con la cabecera real reportada, incluyendo Zeri.
- Controles: cada toque aplica el cambio al valor más reciente del motor, sin esperar una recomposición intermedia.
- Avatares y marcos: catálogo con un solo contenedor desplazable para encabezado, filtros y elementos; no deja la lista con altura cero cuando el encabezado ocupa una pantalla corta.
- Persistencia: habilita respaldo Android con lista explícita de historial, perfiles y preferencias visuales, excluyendo credenciales, identidad del dispositivo y archivos cifrados ligados a la clave local. La transferencia a otro dispositivo no copia la calibración geométrica. Restaura según disponibilidad del respaldo del sistema; no garantiza recuperar datos borrados sin una copia previa. El JSON ahora conserva KDA y modo de partida.
- Archivos: VisionCalibrationConfig, DraftVisionScanner, DraftPickOrderPolicy, LiteRTVisionClassifier, ScannerDebugOverlay, LiteRTEngineViewerDialog, AvatarSelectionDialog, AppThemeManager, BackupRestoreManager, reglas de respaldo y manifiesto; pruebas de perfil/orden y baneos.
- Verificación: catálogos generados y diff revisados localmente. La entrega exige comprobaciones renderizadas, release R8, firma persistente y APK instalado ES/PT antes de fusionar y publicar. No equivale a validación física en todos los modelos.
