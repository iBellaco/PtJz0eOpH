# Auditoría de Coach 1.1.10.270

## Cambios verificados

- La lista de recomendaciones muestra el campeón y su razonamiento; la build ya no se imprime como una tarjeta de texto dentro del listado.
- Al abrir un campeón recomendado, el detalle muestra una build 1 contra 1 cuando existe rival de línea.
- Cuando se conocen los cinco campeones rivales, el detalle añade una build de composición completa calculada con todo el equipo enemigo.
- Las builds siguen resolviendo objetos, runas y hechizos desde los catálogos locales.
- Contra escudos, los magos reciben `Tridente de Oceánida`, y los campeones de daño físico reciben `Colmillo de serpiente`.
- Se conservaron los campos de build anteriores para mantener compatibilidad con el historial y las pruebas existentes.

## Comprobaciones

- Prueba unitaria nueva para la separación 1v1/composición completa y el anti-escudos AP.
- `git diff --check` sin errores.
- Las pruebas Gradle locales no pudieron iniciar porque el entorno no tenía la distribución Gradle 9.3.1 y no tenía acceso de red; la validación final debe ejecutarse en GitHub Actions.

## Revisión manual para el tester

1. Abre Selección de Campeones con un rival de línea visible y pulsa una recomendación.
2. Comprueba que el listado solo muestra el campeón y que la build aparece dentro del detalle.
3. Completa los cinco rivales y vuelve a abrir el campeón: deben aparecer los apartados 1 contra 1 y composición completa.
4. Usa un mago contra una composición con Lulu, Karma o Lux: la sección situacional debe incluir Tridente de Oceánida.
