# Coach 1.1.10.405 — build 1121

## Corrección

El décimo retrato permanece provisional hasta la salida sostenida de selección.
El Hub puede sustituir Sion por Gwen en el mismo puesto, conservando los nueve
anteriores. Tres capturas sin slots durante al menos 800 ms cierran la selección.
Solo el último retrato confirmado temporalmente puede finalizar el draft. El
cierre bloquea reportes tardíos y solicita un único guardado.

## Archivos

- OverlayState.kt, ConfirmedLastPickHud.kt: separar reconocimiento y cierre.
- FloatingOverlayContent.kt: guardar al cierre y actualizar memoria del nuevo retrato.
- DraftSlotLifecycle.kt: ausencia sostenida, con reloj inyectable en pruebas.
- LiteRTVisionClassifier.kt: conservar exclusivamente retratos estables al salir.
- DraftAutoStopTest.kt, DraftSlotLifecycleTest.kt: cambios en ambos lados, pausa,
  ausencia breve, cierre y guardado único.
- AdminSponsorNoticeItem.kt y RuntimeBehaviorTest.kt: reparar referencias y
  expectativa de contador heredadas del cambio previo, necesarias para compilar pruebas.
- fix-error.yml: pruebas dirigidas a este arreglo antes de validación completa.
- AGENTS.md y contrato de reconocimiento: registrar instrucciones del propietario.
- app/build.gradle.kts: versión.

## Verificación

La ejecución dirigida en fix-error debe estar verde antes de integrar en
coach-validacion. La compilación release y las pruebas instaladas corresponden
a validación completa. main queda bajo integración manual del propietario.
No se declara prueba física del cambio Sion/Gwen.
