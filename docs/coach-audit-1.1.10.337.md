# Coach 1.1.10.337 — Build 1053

## Alcance

- El directorio de gestión y actividad comparte una única política de visibilidad:
  excluye perfiles cuya identidad de acceso el servicio confirma como inexistente.
  No elimina perfiles, beneficios, saldos ni historial. No trata un error de permisos
  como una cuenta eliminada. Las reglas impiden falsificar esos campos desde clientes.
- La reconciliación se ejecuta al publicar main y en la tarea horaria existente.
  La eliminación manual no es un evento instantáneo entre servicios: puede tardar
  hasta la próxima ejecución correcta. El listener aplica el cambio en línea y no
  presenta como vigente una lista exclusivamente recuperada de caché.
- El escáner reconoce bandas negras exteriores y transforma los recortes al área
  de juego visible. El visor usa la misma geometría resuelta, sin recalcular la
  cabecera. Los perfiles distinguen orientación y no transfieren ajustes manuales
  a una relación de aspecto nueva durante la sesión.
- Los perfiles guardados también se separan por modelo. Los perfiles antiguos
  solo se recuperan en el modelo que los creó; al reiniciar, una proporción nueva
  no hereda el último ajuste global de otra proporción.
- La captura real de diez selecciones se utiliza como regresión, redimensionada
  proporcionalmente en once superficies. No se sustituyen retratos por plantillas.
- El reconocimiento aliado excluye el distintivo de rol del descriptor y de sus
  referencias, conservando los umbrales de confianza y separación. Los candidatos
  no resueltos reciben alineación fina, sin aprender retratos de la captura de prueba.
- La búsqueda de baneos también utiliza el área visible, reproyectando máscaras
  y resultados al frame completo; no busca dentro de una franja negra superior.
- Se exige contraste interior/exterior al alinear bordes: un fondo azul no es un
  aro. La alineación trabaja a resolución acotada y la extracción no aplica un
  segundo desplazamiento oculto. Los fotogramas dispersos sin un límite de
  contenido observable no se interpretan como áreas de juego recortadas.
- El aro de selección dorado se comprueba a varios radios: la captura de Mel
  demostró que el radio único podía bloquear una identidad de alta confianza.
  Se exige confirmación estable en las once superficies y ambos perfiles, no
  solo que el primer candidato del clasificador sea correcto.
- El espaciado vertical se estima de forma robusta entre aros coherentes. Un
  contorno débil no puede desplazar aisladamente una fila fuera de esa geometría.

## Archivos

Servicio `reconcile-directory.mjs`, pruebas unitarias e integración, workflow de
cuentas, reglas y pruebas de permisos; `ManagedUsersDirectory.kt`, gestión y
actividad; `DraftFrameGeometry.kt`, motor adaptativo, calibración, escáner y visor;
regresiones de directorio/captura, fixture JPG, `test_matchup_rules.py` y versión.

## Verificaciones

31 pruebas Node correctas y ensayo completo de emuladores correcto, incluida la
reconciliación idempotente, restauración de identidad y conservación de saldo.
Validación Android, firma, ofuscación, instalación ES/PT y publicación pendientes
de la ejecución de Actions. Este entorno local no dispone del SDK Android.

## Límites

Las variantes de la imagen prueban escalado y bandas, no todos los diseños que
el juego puede renderizar ni hardware físico. No constituyen una garantía para
cualquier dispositivo. Se conservan los perfiles medidos de Red Magic y Poco
y el ajuste manual. No se publican las capturas con direcciones de correo.
