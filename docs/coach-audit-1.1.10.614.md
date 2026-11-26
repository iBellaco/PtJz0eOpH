# Coach 1.1.10.614 · Reinicio del draft y paletas de los temas de aplicación

Build 1330.

## Correcciones

- Al confirmar «Vaciar», la pantalla del draft inicia una nueva sesión para los dos equipos, el orden de selección, el cara a cara y las recomendaciones. El rival del cara a cara se obtiene de la línea elegida. Los equipos y el análisis usan la misma lista de selecciones en cada actualización.
- El overlay descarta la cola legendaria, la pantalla de carga y el diálogo de cambio de línea al vaciar. Los análisis, avisos y resultados de guardado pendientes quedan asociados al draft que los originó.
- Diez temas de aplicación tienen nuevos fondos, superficies, textos y acentos. White y Black conservan sus escalas neutras. El selector muestra cuatro muestras de cada tema y las tarjetas y los menús de Material usan la paleta aplicada.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/assets/translations_ui_pt.json`
- `app/src/main/java/com/example/ui/components/ThemeCustomizationDialog.kt`
- `app/src/main/java/com/example/ui/theme/AppThemeManager.kt`
- `app/src/main/java/com/example/ui/theme/Theme.kt`
- `app/src/main/java/com/example/ui/screens/MetaAndDraftScreen.kt`
- `app/src/main/java/com/example/service/OverlayState.kt`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/test/java/com/example/DraftChampionSelectionTest.kt`
- `app/src/test/java/com/example/DraftSessionLifecycleTest.kt`
- `app/src/test/java/com/example/ApplicationThemeCatalogTest.kt`
- `app/src/test/java/com/example/ApplicationThemeSelectionTest.kt`
- `docs/coach-audit-1.1.10.614.md`

## Verificación

- La prueba de interfaz selecciona Ahri y Yasuo, cambia el orden de selección, confirma «Vaciar» y comprueba el siguiente enfrentamiento con Orianna y Lux. Se ejecuta en español y portugués.
- Las pruebas del overlay comprueban que un resultado del escáner anterior se descarta incluso si se reactiva el escáner en el segundo draft.
- La prueba de paletas compara las 66 parejas de temas de aplicación y las 156 parejas con regiones en el espacio de color OKLab, también con los acentos intercambiados. El texto y los dos acentos alcanzan un contraste de al menos 4,5 a 1 sobre los tres fondos de cada tema de aplicación.
- Se comprobaron los títulos y descripciones en portugués y los recursos localizados de Android.
- Pasaron las 29 pruebas locales seleccionadas. En la prueba de interfaz se previsualizaron y aplicaron Ember y Mint, se comprobaron los colores de Material y se recuperó el último tema guardado al reinicializar las preferencias.
