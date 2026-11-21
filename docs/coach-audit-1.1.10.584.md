# Coach 1.1.10.584 (1300) · Catálogo ampliado de barras de navegación

Se revisaron las dos referencias de video proporcionadas para la navegación
inferior. Se catalogaron diez composiciones distintas: cuatro variantes de
vidrio del primer video y seis variantes del segundo video (vidrio líquido,
lente neón oscuro, rebote, onda neón, foco y botón flotante). Cada variante
quedó disponible como un estilo independiente; cuando una idea se parecía a
un estilo anterior, se implementó con otra geometría de selección para evitar
que fuera un duplicado visual.

También se añadieron dos propuestas originales que no usan cápsulas, círculos
ni ondas como elemento principal: Cinta modular, formada por paneles inclinados
independientes, y Malla prismática, formada por una retícula de rombos.

Todos los estilos mantienen el color del tema activo, la animación al cambiar
de destino, la notificación del perfil y etiquetas legibles en el tamaño de
previsualización. Se añadieron las traducciones portuguesas y se incrementó la
versión a 1.1.10.584 (1300).

## Archivos modificados

- `app/src/main/java/com/example/ui/theme/AppThemeManager.kt`
- `app/src/main/java/com/example/ui/components/ShowcaseScreen.kt`
- `app/src/main/java/com/example/ui/components/VideoReferenceCoachNavBar.kt`
- `app/src/main/assets/translations_ui_pt.json`
- `app/src/test/java/com/example/CoachNavStyleTest.kt`
- `app/build.gradle.kts`

## Verificaciones

- JSON de traducciones válido.
- Auditoría de portugués sin hallazgos de texto español.
- `git diff --check` sin errores.
- Pruebas locales de estilos persistentes, renderizado de todas las barras,
  legibilidad de consejos y auditoría portuguesa completadas correctamente.
- Se revisaron capturas renderizadas de las variantes de vidrio, neón,
  geometría modular y malla prismática para comprobar que el icono, el título y
  la selección siguen visibles.
