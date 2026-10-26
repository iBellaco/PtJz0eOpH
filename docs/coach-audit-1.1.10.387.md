# Coach 1.1.10.387 — build 1103

La ejecución 40298348367 encontró un fallo nuevo en sequence-42-ally-first:
en el paso 4 falta ORNN (ML Kit devuelve 6ORNN/OORNN). Persiste VI en
1920×822; 1920×924 y 1920×889 siguen aprobando los nueve títulos y Teemo.

La ampliación de la máscara a bordes claros había alterado el procesamiento
general. Se restaura la detección dorada y la expansión anteriores por defecto.
Los bordes claros solo se eliminan con includePaleEdges=true en la recuperación
de títulos pendientes. Esa recuperación procesa ahora la banda COMPLETA antes
de recortar el margen: un emblema cortado ya no conserva el núcleo alto que
permite reconocer su ala como emblema en vez de letra. Los colores originales
y las variantes previas permanecen disponibles; no se aceptan 6ORNN ni OORNN
como alias nuevos ni se utiliza el retrato para completar títulos faltantes.

La regresión sintética comprueba que la máscara general conserva su conducta
y que solo la alternativa explícita elimina el borde claro conectado. fix-error
ya incluye el caso 42 completo, los otros cuatro casos conocidos y los 142 nombres.

Archivos: DraftImagePreprocessor.kt, DraftNameAtlas.kt, DraftNameAtlasTest.kt,
app/build.gradle.kts y este informe. Verificación local: 9/9 pruebas Python y
diff limpio; Kotlin y OCR nativo pendientes de Actions por ausencia de SDK.
La evidencia instalada sigue siendo de emulador e imágenes, no prueba física.
No se modifica main ni coach-validacion.
