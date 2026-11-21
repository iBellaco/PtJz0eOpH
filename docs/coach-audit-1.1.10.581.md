# Coach 1.1.10.581 (1297) · Alineación de los retratos con el visor activo

La prueba de selección sobre capturas originales detectó 12 centros fuera de la tolerancia de 6 px en tres escenas. Con el visor activo, la búsqueda dependía del color del equipo y del contraste dentro del retrato. Al atenuarse el aro del juego, algunas coincidencias parciales en el dibujo desplazaban los recortes. En el POCO la columna rival quedaba a unos 60 px de su posición.

En DraftRingAlignment.kt la búsqueda con el visor activo mide los cambios de intensidad de los canales de color alrededor de la circunferencia. Descarta el contorno que dibuja el visor y pide mayor cobertura angular cuando un candidato se superpone con él. Esto permite localizar un puesto vacío por su aro y conservar el centro anterior cuando el borde visible es insuficiente. El cálculo reutiliza el búfer del fotograma y dos filas auxiliares. La versión se actualizó en app/build.gradle.kts.

## Pruebas con las capturas originales

DraftNativePanelTest compara los centros en ocho archivos con sus dimensiones y huellas registradas. Los 70 puestos de las capturas de 1280 × 579 y los cinco puestos rivales visibles de la captura de 1280 × 591 quedaron dentro de 6 px. La desviación máxima fue de 5,70 px. En la escena POCO el máximo fue de 1,58 px. El informe identifica los cinco puestos aliados ocultos en la captura del S26. La comprobación en vivo en los teléfonos sigue pendiente.

La revisión final pasó 118 pruebas locales en 17 suites, incluidas las del paso que falló en CI y las regresiones de alineación, visor y baneos. También pasaron las verificaciones de contenido del parche y recursos de idioma.
