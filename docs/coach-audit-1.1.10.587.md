# Coach 1.1.10.587 (1303) · Navegación inmersiva por tema

## Problema corregido

La barra `Pestañas cálidas` conservaba colores marrones propios de su referencia
aunque la aplicación tuviera otro tema activo. Además, `Líneas neón` y `Foco
radial` seguían apareciendo como opciones de navegación.

## Cambio realizado

- `Pestañas cálidas` ahora toma el fondo, acento, estado seleccionado y estado
  inactivo del tema de aplicación activo, incluida su vista previa.
- Se retiraron `Líneas neón` (`dark_neon_lines`) y `Foco radial` (`spotlight`)
  del catálogo.
- Las preferencias antiguas que contengan esos identificadores vuelven de forma
  segura al estilo original, sin alterar los temas regionales.
- El catálogo pasa de 28 a 26 estilos, conservando el resto de las animaciones.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/ui/components/ShowcaseScreen.kt`
- `app/src/main/java/com/example/ui/components/VideoCoachNavBar.kt`
- `app/src/main/java/com/example/ui/components/VideoReferenceCoachNavBar.kt`
- `app/src/main/java/com/example/ui/theme/AppThemeManager.kt`
- `app/src/test/java/com/example/CoachNavStyleTest.kt`

## Verificaciones

- Se añadieron pruebas para la compatibilidad de preferencias retiradas y el
  catálogo de 26 estilos.
- Se ejecutará la validación unitaria y de renderizado de navegación antes de
  enviar esta modificación al flujo de `fix-error`.
