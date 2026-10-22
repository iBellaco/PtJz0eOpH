# Coach 1.1.10.338 — Build 1054

## Cambios

- Cabecera del visor separada de sus acciones. Controles de posición, tamaño,
  paso, envío y reinicio en bloques adaptables con objetivos de al menos 48dp.
- Ambos calibradores envían el ajuste actual por la conversación de soporte,
  con confirmación tras respuesta del servicio y errores recuperables. No se
  adjuntan capturas automáticamente. El mensaje se puede copiar desde soporte.
- Bloque de coordenadas versionado y acotado, sin identificadores de cuenta.
  Configuración y dimensiones se leen en una instantánea sincronizada; el bloque
  copiable conserva todas las listas individuales de posiciones y tamaños.
- Directorio compartido con estados sincronizando, vigente, sin conexión y error;
  hora de recepción del último snapshot, no una falsa hora de verificación de identidad.
- Exclusión de cuentas confirmadas inexistentes en moderación, creadores activos
  y destinatarios masivos, sin borrar builds ni historial. El chequeo de identidad
  sigue la programación horaria existente; no se promete eliminación instantánea.
  Las autorías históricas de builds públicas no requieren ni exponen el directorio
  privado; el filtro de cuentas activas de creadores se aplica en las vistas de gestión.
- Más tamaños de reconocimiento y baneos, matriz geométrica y cambios del centro.
  Se mantienen los perfiles medidos y los umbrales de confianza.
  La permutación de retratos detectó una regresión: dos aros incoherentes podían
  estirar la rejilla y desplazar recortes hasta 22px. En una columna ya ubicada,
  hacen falta tres aros para cambiar espaciado; dos solo admiten traslación coherente.
  Si dos aros independientes prueban que la columna horizontal pertenece a otro
  formato, se conserva la recuperación inicial de espaciado del perfil extranjero.
  Así no se confunde estabilizar una geometría observada con congelar un preset de otro teléfono.
  Ambos calibradores comparten los mismos controles y la etiqueta de tamaño
  ocupa el ancho disponible, evitando recorte por redondeo de medida del texto.
- Diagnóstico seguro por clase de error y enlace al paso fallido. Evidencia final
  con commit, ejecución y estado de cada job en el artefacto delivery-evidence;
  los jobs omitidos no se presentan como pruebas nuevas ejecutadas.
  Los fallos de regresión muestran el mensaje de la aserción, con correos y enlaces
  censurados, también en anotaciones visibles. El servicio de cuentas agrega
  categorías seguras de error sin identidades, mensajes privados ni tokens.
- Auditoría anterior corregida con la publicación real, sin dejarla como pendiente.

## Archivos

Calibradores, CalibrationControls, CalibrationReport, SupportReplyDialog,
ManagedUsersDirectory, DirectorySyncLabel, gestión/actividad/moderación/creadores
y mensajería; recursos ES/PT; pruebas de coordenadas, permisos, controles,
directorio y DraftReferenceFrameTest; workflow, diagnóstico y sus pruebas;
inventario de recuperación, manuales, bitácoras y versión.

## Comprobaciones y evidencia terminal

La entrega exige las pruebas Android, firma persistente, R8 real e instalación
ES/PT antes de fusionar. El resultado terminal se obtiene de la ejecución ligada
al commit de esta versión y del artefacto delivery-evidence, no de esta nota escrita
antes de compilar. No considerar una prueba aprobada por estar mencionada aquí.

## Pendientes explícitos

S26 requiere comprobación física. Red Magic y Poco funcionan según el propietario;
las simulaciones no certifican cualquier dispositivo ni cualquier tamaño.
La activación de respaldo productivo y su ensayo aislado requieren primero una
cotización y aprobación del costo y destino. El propietario autorizó consultar
el costo, no activar cobros. No se han creado servicios de pago ni restaurado datos reales.
