# Auditoría Coach 1.1.10.283 (build 999)

## Cambios

- Se retiró el botón manual de actualización de la vista pública de clasificación.
- La vista mantiene un pulso de actividad y refresca su estado local cada segundo; las tasas solo se reemplazan cuando una fuente publica una observación nueva.
- El resumen de sincronización muestra el estado automático y la hora de la última consulta con una precisión de segundos.
- El punto «actual» de cada gráfica tiene un pulso visual para distinguir una vista activa de una pantalla congelada.

## Criterio de datos

Las fuentes externas publican estadísticas por lotes o mediante informes diarios. No se generan porcentajes artificiales cada segundo. La actualización de un segundo confirma que la pantalla sigue observando el estado; el valor cambia únicamente después de una respuesta nueva y válida.

## Verificación

- Se revisaron las dependencias y los usos del control eliminado.
- Se verificará compilación, pruebas unitarias, R8 y prueba instalada en la ejecución de validación antes de publicar.
