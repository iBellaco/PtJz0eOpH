# Auditoría Coach 1.1.10.266

- Build: 982.
- Draft: se corrigió la relación de Línea Central entre Akali y Orianna. Orianna queda marcada como enfrentamiento desfavorable contra Akali y ya no puede entrar en las recomendaciones principales para ese rival.
- Líneas: cuando hay al menos cinco campeones naturales para la línea elegida, el ranking usa sus roles principales. Los flex quedan como respaldo de un catálogo incompleto y no desplazan a un campeón de la línea por tener una tier global más alta.
- Puntuación: aumentó el peso del uno contra uno confirmado y de las debilidades contra el resto de la composición. Las sinergias verificadas, la falta de primera línea, la ausencia de iniciación, el riesgo de doble asesino, el daño contra tanques y la necesidad de entrar contra poke ahora modifican el resultado.
- Contexto: si el slot rival no llega explícito pero solo hay un rival con el rol natural correspondiente, el análisis lo usa como oponente de línea. Así no se activa el modo de primera selección por una lectura parcial.
- Texto: las recomendaciones explican cuándo completan la primera línea, castigan una composición resistente o protegen contra varios asesinos.
- Pruebas: se agregaron regresiones para Akali/Orianna, la sinergia Orianna/Malphite y una composición rival con dos asesinos. Se conservaron las comprobaciones existentes de catálogo, portugués y draft.
- Archivos: DraftScoringPolicy, WildRiftRepository, ReviewedLaneMatchups, DraftRecommendationQualityTest, versión Gradle.
- La comprobación del APK release, firma persistente, ofuscación R8, pantallas en español y portugués y los flujos de publicación se ejecutará antes de fusionar.
- La validación automatizada es la que autoriza la publicación; la ejecución local quedó limitada por la ausencia de la distribución Gradle en este entorno.
