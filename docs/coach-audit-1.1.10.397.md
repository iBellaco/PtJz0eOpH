# Coach 1.1.10.397 — build 1113

Reporte físico del tester en POCO: baneos y primeros picks lentos, jungla
mostrado como rival y ADC como jungla. Modelo, campeones y orden sin confirmar.

Cambios: BannedPortraitScanner comparte la cadencia de 250 ms del trabajador,
eliminando el bloqueo interno de 1500 ms. Se mantienen dos observaciones para
confirmar. DraftSyncCadence espacia metadatos a seis segundos para dejar más
fotogramas a la lectura de títulos. Esto no garantiza una latencia física.

DraftVisionScanner publica líneas aliadas observadas o deducidas exclusivamente
de cuatro líneas distintas, sin completar por afinidad del campeón. El décimo
visual, incluido el camino directo, requiere el lado respaldado por nueve títulos;
un orden tentativo o el conteo del Hub no autoriza una confirmación visual.

Regresiones: cadencia, conservación de ADC/jungla en las 20 distribuciones de
slots y pruebas existentes de ambos órdenes y baneos se incluyen en fix-error.
La suite Android local requiere SDK ausente en este entorno. Pendiente resultado
real en GitHub y nueva prueba física en POCO; no se afirma resuelto en teléfono.
