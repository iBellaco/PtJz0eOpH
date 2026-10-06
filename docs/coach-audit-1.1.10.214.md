# Auditoría Técnica y Registro de Modificaciones — Versión 1.1.10.214 (Build 930)

## Resumen Ejecutivo
1. **Consejos de objetos core revisados:**
   - Se sustituyeron los tiempos de compra genéricos por decisiones específicas para todos los objetos core presentes en las builds incluidas.
   - Cada consejo mantiene la función real del objeto y añade una condición concreta para completar la compra.

2. **Runas y hechizos con decisiones específicas:**
   - Se cubrieron explícitamente todas las runas usadas por las builds oficiales, incluyendo su condición real de activación y uso.
   - Aplastar dejó de caer en el consejo genérico de hechizos.
   - Destello continúa sin mostrar tarjeta de consejo, tal como requiere la interfaz.

3. **Botas Nivel 2 y Nivel 3 corregidas:**
   - Las botas ya no reutilizan descripciones guardadas de otra selección.
   - Cada una de las 7 botas Nivel 2 y sus 7 evoluciones Nivel 3 genera su propio consejo contextual.
   - Se corrigió específicamente la regresión donde Botas de mercurio podía mostrar el consejo de Grebas codiciosas.
   - Las alternativas situacionales de botas también usan el consejo correspondiente a la bota seleccionada.

4. **Etiquetas de mapas en el catálogo de hechizos:**
   - El encabezado textual “Mapa/Mapas aplicables/disponibles” se separó de la descripción mecánica.
   - Wild Rift y Abismo de los Lamentos se muestran como etiquetas visuales independientes en la vista detallada y en el modal del hechizo.
   - El formato reconoce variantes en español y portugués.

5. **Cobertura de regresión añadida:**
   - Prueba exhaustiva sobre los objetos core, runas, hechizos y botas realmente usados por las 300 builds incluidas.
   - Prueba específica para impedir que Botas de mercurio vuelva a reutilizar el consejo de Grebas codiciosas.
   - Pruebas del parser de mapas para múltiples mapas, un solo mapa y localización portuguesa.

## Archivos Modificados
- `app/src/main/java/com/example/util/BuildElementAdvice.kt`
- `app/src/main/java/com/example/util/SpellCatalogFormatting.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/ui/screens/MetaAndDraftScreen.kt`
- `app/src/test/java/com/example/BuildElementAdviceCoverageTest.kt`
- `app/src/test/java/com/example/SpellCatalogFormattingTest.kt`
- `app/build.gradle.kts`
- `.github/workflows/build-apk.yml`
- `docs/coach-audit-1.1.10.214.md`

## Verificación
- Cobertura automática añadida para las 300 builds y 142 campeones del catálogo incluido.
- La validación de compilación, pruebas unitarias y APK release se ejecuta mediante el flujo de validación del repositorio antes de fusionar a `main`.
