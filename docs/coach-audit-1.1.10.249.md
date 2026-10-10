# Auditoría Coach 1.1.10.249

- Versión: 1.1.10.249
- Build: 965
- Problema: al abrir runas situacionales, el diálogo mantenía el texto del consejo en un estado compartido y separado de la runa seleccionada. Esto podía dejar desincronizados el efecto mostrado y el consejo.
- Cambio: la selección del diálogo ahora conserva juntas la identidad de la runa y su consejo específico. La prueba de interfaz comprueba que cada alternativa muestre su nombre, efecto de catálogo y nota de build correspondientes en español y portugués.
- Archivos modificados: `app/build.gradle.kts`, `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`, `app/src/test/java/com/example/BuildCoachingRenderedTest.kt`.
- Pruebas: `git diff --check` completado. La prueba local no pudo arrancar porque el entorno no permite descargar Gradle 9.3.1; ejecutar la suite y las comprobaciones de publicación en GitHub Actions.
