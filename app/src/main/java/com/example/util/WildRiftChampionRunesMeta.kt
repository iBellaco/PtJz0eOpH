package com.example.util

import com.example.model.Champion
import com.example.model.DamageType
import com.example.model.LaneRole

/**
 * Catálogo y generador oficial de Runas de Wild Rift (Meta actualizado según WildRiftFire, WildRiftCore y Meta Pro Global).
 * Cada campeón posee 2 combinaciones distintas y optimizadas:
 * - Opción 1: Build Principal / Meta Estándar (1 Runa Clave + 4 Secundarias de nuestro catálogo oficial).
 * - Opción 2: Build Alternativa / Situacional contra composiciones específicas (Poke, Burst, Tanques o Sustain).
 */
object WildRiftChampionRunesMeta {

    data class ChampionRunePair(
        val opt1Title: String,
        val option1: List<String>,
        val opt2Title: String,
        val option2: List<String>
    )

    private val championSpecificRunes: Map<String, ChampionRunePair> = mapOf(
        // --- TOP / BARON LANE ---
        "aatrox" to ChampionRunePair(
            opt1Title = "Duelo & Conquistador (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Anti-Tanque & Sustain",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo")
        ),
        "darius" to ChampionRunePair(
            opt1Title = "Conquistador & Sangrado (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Movilidad & Persecución (Fantasmal)",
            option2 = listOf("Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Trascendencia", "Triunfo")
        ),
        "garen" to ChampionRunePair(
            opt1Title = "Garras & Demoler (Meta)",
            opt2Title = "Conquistador & All-in",
            option1 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"),
            option2 = listOf("Conquistador", "Brutal", "Leyenda: Tenacidad", "Último Esfuerzo", "Inquebrantable")
        ),
        "sett" to ChampionRunePair(
            opt1Title = "Conquistador & Traspaso (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Fuerzas Renovadas"),
            opt2Title = "Garras & Gran Escudo",
            option2 = listOf("Garras del Inmortal", "Demoler", "Revestimiento de Huesos", "Sobrecrecimiento", "Brutal")
        ),
        "fiora" to ChampionRunePair(
            opt1Title = "Puntos Vitales & Conquistador",
            option1 = listOf("Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Garras del Inmortal (Split-Push)",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Celeridad")
        ),
        "camille" to ChampionRunePair(
            opt1Title = "Garras & Tradeos Cortos (Meta)",
            option1 = listOf("Garras del Inmortal", "Demoler", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"),
            opt2Title = "Primer Golpe (Burst de Q2)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "jax" to ChampionRunePair(
            opt1Title = "Compás Letal & Duelo Continuo",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Garras del Inmortal (Línea Pasiva)",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo")
        ),
        "irelia" to ChampionRunePair(
            opt1Title = "Conquistador & Cargas (Meta)",
            option1 = listOf("Conquistador", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Compás Letal (Velocidad Pura)",
            option2 = listOf("Compás Letal", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Fuerzas Renovadas")
        ),
        "riven" to ChampionRunePair(
            opt1Title = "Conquistador & Combos (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (Burst Letal)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Trascendencia")
        ),
        "renekton" to ChampionRunePair(
            opt1Title = "Conquistador & Furia (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Garras del Inmortal (Sustain)",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Brutal")
        ),
        "ornn" to ChampionRunePair(
            opt1Title = "Garras & Forja (Meta)",
            option1 = listOf("Garras del Inmortal", "Fuente de Vida", "Fuerzas Renovadas", "Sobrecrecimiento", "Perseverancia"),
            opt2Title = "Soberano Gélido (Teamfight CC)",
            option2 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Inquebrantable", "Trascendencia")
        ),
        "sion" to ChampionRunePair(
            opt1Title = "Garras & Demolición Máxima",
            option1 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"),
            opt2Title = "Irrupción de Fase (Movilidad H1)",
            option2 = listOf("Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Trascendencia", "Demoler")
        ),
        "malphite" to ChampionRunePair(
            opt1Title = "Cometa Arcano (Poke H1 AP)",
            option1 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Fuerzas Renovadas"),
            opt2Title = "Garras del Inmortal (Tanque Armor)",
            option2 = listOf("Garras del Inmortal", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Banda de Maná")
        ),
        "gwen" to ChampionRunePair(
            opt1Title = "Pies Veloces & Tijeretazos (Meta)",
            option1 = listOf("Pies Veloces", "Triunfo", "Fuerzas Renovadas", "Perseverancia", "Leyenda: Linaje"),
            opt2Title = "Conquistador AP (Duelo Extendido)",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos")
        ),
        "jayce" to ChampionRunePair(
            opt1Title = "Primer Golpe (Aceleración de Oro)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Conquistador (Tradeos Extendidos)",
            option2 = listOf("Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Trascendencia")
        ),
        "teemo" to ChampionRunePair(
            opt1Title = "Aery (Hostigamiento & Veneno)",
            option1 = listOf("Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Colección de Globos Oculares"),
            opt2Title = "Pies Veloces (Kiteo & Sustain)",
            option2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Fuerzas Renovadas")
        ),
        "kennen" to ChampionRunePair(
            opt1Title = "Electrocutar (Burst Eléctrico)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Celeridad"),
            opt2Title = "Primer Golpe (Teamfights Definitiva)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Banda de Maná")
        ),
        "shen" to ChampionRunePair(
            opt1Title = "Garras del Inmortal (Tradeos H1)",
            option1 = listOf("Garras del Inmortal", "Fuente de Vida", "Fuerzas Renovadas", "Sobrecrecimiento", "Inquebrantable"),
            opt2Title = "Guardián (Soporte Global Definitiva)",
            option2 = listOf("Guardián", "Fuente de Vida", "Revestimiento de Huesos", "Perseverancia", "Trascendencia")
        ),
        "volibear" to ChampionRunePair(
            opt1Title = "Compás Letal (Tormenta H2)",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Garras del Inmortal (Tanque)",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo")
        ),
        "nasus" to ChampionRunePair(
            opt1Title = "Pies Veloces (Sustain H1 Farm)",
            option1 = listOf("Pies Veloces", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Fuerzas Renovadas"),
            opt2Title = "Garras del Inmortal (Demoler Torres)",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Banda de Maná")
        ),
        "urgot" to ChampionRunePair(
            opt1Title = "Conquistador (Ametralladora H2)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Garras del Inmortal (Escalado)",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo")
        ),
        "kayle" to ChampionRunePair(
            opt1Title = "Compás Letal (Hiper-Escalado Lv15)",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Sobrecrecimiento"),
            opt2Title = "Pies Veloces (Supervivencia Temprana)",
            option2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Fuerzas Renovadas")
        ),
        "mordekaiser" to ChampionRunePair(
            opt1Title = "Conquistador (Reino de la Muerte)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Garras del Inmortal (Sustain)",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Trascendencia")
        ),
        "singed" to ChampionRunePair(
            opt1Title = "Conquistador (Veneno Continuo)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Celeridad"),
            opt2Title = "Irrupción de Fase (Velocidad Extrema)",
            option2 = listOf("Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Banda de Maná", "Fuerzas Renovadas")
        ),
        "dr_mundo" to ChampionRunePair(
            opt1Title = "Garras del Inmortal (Vida Infinita)",
            option1 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Inquebrantable"),
            opt2Title = "Pies Veloces (Kiteo de Cuchillas)",
            option2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Fuerzas Renovadas")
        ),
        "ambessa" to ChampionRunePair(
            opt1Title = "Conquistador & Deslizamientos (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Impacto Repentino"),
            opt2Title = "Primer Golpe (Iniciación Definitiva Letal)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),

        // --- MID LANE ---
        "ahri" to ChampionRunePair(
            opt1Title = "Electrocutar & Encanto (Meta)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Primer Golpe (Aceleración de Objetos)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Trascendencia")
        ),
        "zed" to ChampionRunePair(
            opt1Title = "Conquistador & Duelista (Meta)",
            option1 = listOf("Conquistador", "Fervor de Batalla", "Golpe de Gracia", "Leyenda: Presteza", "Trascendencia"),
            opt2Title = "Primer Golpe (Snowball Rápido)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Soberbia", "Cazador Ingenioso", "Trascendencia")
        ),
        "yasuo" to ChampionRunePair(
            opt1Title = "Compás Letal & Tempestad (Meta)",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Conquistador (Duelo contra Tanques)",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Fuerzas Renovadas")
        ),
        "yone" to ChampionRunePair(
            opt1Title = "Garras del Inmortal & Duelo (Meta)",
            option1 = listOf("Garras del Inmortal", "Impacto Repentino", "Golpe de Gracia", "Leyenda: Presteza", "Fuerzas Renovadas"),
            opt2Title = "Conquistador (Peleas Grupales Definitiva)",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Sobrecrecimiento")
        ),
        "syndra" to ChampionRunePair(
            opt1Title = "Primer Golpe & Esferas (Meta)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Cometa Arcano (Poke de Rango)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta")
        ),
        "lux" to ChampionRunePair(
            opt1Title = "Cometa Arcano (Hostigamiento H3)",
            option1 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta"),
            opt2Title = "Primer Golpe (Burst Definitiva Láser)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
        ),
        "orianna" to ChampionRunePair(
            opt1Title = "Cometa Arcano (Control de Esfera)",
            option1 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta"),
            opt2Title = "Irrupción de Fase (Movilidad en TF)",
            option2 = listOf("Irrupción de Fase", "Banda de Maná", "Trascendencia", "Celeridad", "Triunfo")
        ),
        "hwei" to ChampionRunePair(
            opt1Title = "Primer Golpe & Arte Devastador (Meta)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Cometa Arcano (Poke de Largo Alcance)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta")
        ),
        "akali" to ChampionRunePair(
            opt1Title = "Electrocutar & Velo de Sombras",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Fuerzas Renovadas"),
            opt2Title = "Conquistador (Duelos Extendidos)",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos")
        ),
        "katarina" to ChampionRunePair(
            opt1Title = "Conquistador (Dagas en Área)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino"),
            opt2Title = "Electrocutar (Burst Rápido)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "akshan" to ChampionRunePair(
            opt1Title = "Fortalecimiento & Gancho Letal (Meta)",
            option1 = listOf("Fortalecimiento", "Brutal", "Golpe de Gracia", "Leyenda: Presteza", "Revestimiento de Huesos"),
            opt2Title = "Pies Veloces (Movilidad & Kiting)",
            option2 = listOf("Pies Veloces", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Fuerzas Renovadas")
        ),
        "vex" to ChampionRunePair(
            opt1Title = "Electrocutar & Pesimismo (Meta)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Primer Golpe (Reseteo Definitiva Letal)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Trascendencia")
        ),
        "veigar" to ChampionRunePair(
            opt1Title = "Primer Golpe (Acumulación H1 + Oro)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Electrocutar (Jaula + H2+H1+Definitiva)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Trascendencia")
        ),
        "twisted_fate" to ChampionRunePair(
            opt1Title = "Electrocutar (Carta Dorada + H1)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Cometa Arcano / Primer Golpe",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Trascendencia")
        ),
        "vladimir" to ChampionRunePair(
            opt1Title = "Irrupción de Fase (Movilidad H1 Carga)",
            option1 = listOf("Irrupción de Fase", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Colección de Globos Oculares"),
            opt2Title = "Conquistador (Sustain Masivo Definitiva)",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos")
        ),
        "kassadin" to ChampionRunePair(
            opt1Title = "Primer Golpe & Escalado Nivel 13",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Pies Veloces (Supervivencia Early)",
            option2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Fuerzas Renovadas")
        ),
        "ekko" to ChampionRunePair(
            opt1Title = "Electrocutar & Resonancia Z (Meta)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Primer Golpe (Burst de H3 + H1 + AA)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Trascendencia")
        ),
        "fizz" to ChampionRunePair(
            opt1Title = "Electrocutar & Tiburón Definitiva (Meta)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"),
            opt2Title = "Primer Golpe (All-in Letal)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Banda de Maná")
        ),
        "aurelion_sol" to ChampionRunePair(
            opt1Title = "Primer Golpe (Aliento Solar H1)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Cometa Arcano (Hostigamiento H3)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Colección de Globos Oculares")
        ),
        "zoe" to ChampionRunePair(
            opt1Title = "Electrocutar (Burbuja H3 + Estrella H1)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Primer Golpe (One-Shot a Larga Distancia)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Trascendencia")
        ),
        "ziggs" to ChampionRunePair(
            opt1Title = "Cometa Arcano (Bombardeo H1/H3)",
            option1 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta"),
            opt2Title = "Primer Golpe (Demolición H2 + Definitiva)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
        ),
        "brand" to ChampionRunePair(
            opt1Title = "Cometa Arcano (Llamarada & Quemadura)",
            option1 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Colección de Globos Oculares"),
            opt2Title = "Primer Golpe (Teamfight Definitiva Letal)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
        ),
        "galio" to ChampionRunePair(
            opt1Title = "Electrocutar (Burst AP + Taunt)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Fuerzas Renovadas"),
            opt2Title = "Soberano Gélido (Tanque / Frontline)",
            option2 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Trascendencia")
        ),
        "annie" to ChampionRunePair(
            opt1Title = "Electrocutar & Tibbers (Burst)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Cometa Arcano (Poke de Desintegración)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta")
        ),

        // --- DRAGON LANE (ADCs) ---
        "jinx" to ChampionRunePair(
            opt1Title = "Compás Letal & ¡A Toope! (Meta)",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Sobrecrecimiento"),
            opt2Title = "Pies Veloces (Sustain & Velocidad)",
            option2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Derribado", "Revestimiento de Huesos")
        ),
        "kaisa" to ChampionRunePair(
            opt1Title = "Compás Letal & Plasma (Meta)",
            option1 = listOf("Compás Letal", "Impacto Repentino", "Golpe de Gracia", "Leyenda: Linaje", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (Burst de Evolución H1)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "kai_sa" to ChampionRunePair(
            opt1Title = "Compás Letal & Plasma (Meta)",
            option1 = listOf("Compás Letal", "Impacto Repentino", "Golpe de Gracia", "Leyenda: Linaje", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (Burst de Evolución H1)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "caitlyn" to ChampionRunePair(
            opt1Title = "Primer Golpe & Tiros a la Cabeza",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"),
            opt2Title = "Compás Letal (Rango & Velocidad)",
            option2 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Sobrecrecimiento")
        ),
        "ezreal" to ChampionRunePair(
            opt1Title = "Conquistador & Disparo Místico",
            option1 = listOf("Conquistador", "Impacto Repentino", "Golpe de Gracia", "Leyenda: Presteza", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (Oro Rápido H1)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
        ),
        "vayne" to ChampionRunePair(
            opt1Title = "Compás Letal & Proyectiles de Plata",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Pies Veloces (Fase de Líneas Segura)",
            option2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Derribado", "Fuerzas Renovadas")
        ),
        "varus" to ChampionRunePair(
            opt1Title = "Compás Letal (DPS On-Hit & Crítico)",
            option1 = listOf("Compás Letal", "Brutal", "Derribado", "Leyenda: Presteza", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (AP Burst / Letalidad H1)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
        ),
        "lucian" to ChampionRunePair(
            opt1Title = "Conquistador & Pistolero (Meta)",
            option1 = listOf("Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (Burst de Doble Disparo)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
        ),
        "samira" to ChampionRunePair(
            opt1Title = "Conquistador & Rango S (Meta)",
            option1 = listOf("Conquistador", "Brutal", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Triunfo & Impacto Repentino",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino")
        ),
        "jhin" to ChampionRunePair(
            opt1Title = "Pies Veloces (Movilidad 4º Tiro)",
            option1 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (Burst de Granada H1)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Trascendencia")
        ),
        "tristana" to ChampionRunePair(
            opt1Title = "Compás Letal & Carga Explosiva",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Sobrecrecimiento"),
            opt2Title = "Primer Golpe (All-in Salto H2)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "draven" to ChampionRunePair(
            opt1Title = "Conquistador & Hachas Giratorias",
            option1 = listOf("Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (Snowball Pasiva)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "miss_fortune" to ChampionRunePair(
            opt1Title = "Primer Golpe & Balacera Definitiva (Meta)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Cometa Arcano (Poke de Lluvia H3)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta")
        ),
        "xayah" to ChampionRunePair(
            opt1Title = "Compás Letal & Plumas Rápido (Meta)",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Sobrecrecimiento"),
            opt2Title = "Primer Golpe (Burst de H3 Plumaje)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "ashe" to ChampionRunePair(
            opt1Title = "Compás Letal & Flechas de Escarcha",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Sobrecrecimiento"),
            opt2Title = "Cometa Arcano (Poke H2 de Utilidad)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Triunfo")
        ),
        "zeri" to ChampionRunePair(
            opt1Title = "Compás Letal & Chispas (Meta)",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Sobrecrecimiento"),
            opt2Title = "Pies Veloces (Kiteo & Velocidad)",
            option2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Derribado", "Revestimiento de Huesos")
        ),
        "sivir" to ChampionRunePair(
            opt1Title = "Compás Letal & Rebote H2 (Meta)",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Banda de Maná"),
            opt2Title = "Primer Golpe (Boomerang H1 Letal)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
        ),
        "nilah" to ChampionRunePair(
            opt1Title = "Conquistador & Júbilo (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Compás Letal (Velocidad de Látigo)",
            option2 = listOf("Compás Letal", "Triunfo", "Leyenda: Presteza", "Golpe de Gracia", "Fuerzas Renovadas")
        ),
        "kalista" to ChampionRunePair(
            opt1Title = "Compás Letal & Saltos Marciales",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Conquistador (Desgarrar H3 Stack)",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Presteza", "Golpe de Gracia", "Sobrecrecimiento")
        ),

        // --- JUNGLE ---
        "lee_sin" to ChampionRunePair(
            opt1Title = "Conquistador & Onda Sónica (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Golpe de Gracia", "Impacto Repentino"),
            opt2Title = "Electrocutar (Insec Burst Rápido)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "kayn" to ChampionRunePair(
            opt1Title = "Primer Golpe (Asesino Sombrío)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Trascendencia"),
            opt2Title = "Conquistador (Rhaast / Darkin)",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos")
        ),
        "viego" to ChampionRunePair(
            opt1Title = "Conquistador & Posesiones (Meta)",
            option1 = listOf("Conquistador", "Impacto Repentino", "Golpe de Gracia", "Leyenda: Presteza", "Concentración Absoluta"),
            opt2Title = "Compás Letal (Duelo Directo)",
            option2 = listOf("Compás Letal", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos")
        ),
        "khazix" to ChampionRunePair(
            opt1Title = "Primer Golpe & Aislamiento (Meta)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"),
            opt2Title = "Electrocutar (Burst Instantáneo)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Cazador Incesante")
        ),
        "vi" to ChampionRunePair(
            opt1Title = "Conquistador & Rompebóvedas (Meta)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Electrocutar (Gankeo de Asalto Definitiva)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "evelynn" to ChampionRunePair(
            opt1Title = "Electrocutar & Sombra Demoniaca",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Trascendencia"),
            opt2Title = "Primer Golpe (One-Shot en Sigilo)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Trascendencia")
        ),
        "master_yi" to ChampionRunePair(
            opt1Title = "Compás Letal & Estilo Wuju (Meta)",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Sobrecrecimiento"),
            opt2Title = "Conquistador (Sustain de Duelo H1)",
            option2 = listOf("Conquistador", "Brutal", "Leyenda: Presteza", "Último Esfuerzo", "Inquebrantable")
        ),
        "talon" to ChampionRunePair(
            opt1Title = "Primer Golpe & Salto de Muros",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"),
            opt2Title = "Electrocutar (Burst Sangriento)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Cazador Incesante")
        ),
        "kindred" to ChampionRunePair(
            opt1Title = "Compás Letal & Marcas de Caza",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino"),
            opt2Title = "Conquistador (Resistencia Cordero Definitiva)",
            option2 = listOf("Conquistador", "Brutal", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos")
        ),
        "amumu" to ChampionRunePair(
            opt1Title = "Soberano Gélido & Maldición (Meta)",
            option1 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Trascendencia"),
            opt2Title = "Conquistador (Llantos AP Quemadura)",
            option2 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Golpe de Gracia", "Fuerzas Renovadas")
        ),
        "hecarim" to ChampionRunePair(
            opt1Title = "Conquistador & Carga Devastadora",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Golpe de Gracia", "Celeridad"),
            opt2Title = "Irrupción de Fase (Velocidad AD)",
            option2 = listOf("Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Trascendencia", "Triunfo")
        ),
        "jarvan_iv" to ChampionRunePair(
            opt1Title = "Conquistador & Estandarte Demaciano",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Electrocutar (Combo H3-H1-Definitiva Burst)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "xin_zhao" to ChampionRunePair(
            opt1Title = "Compás Letal & Golpe de 3 Garras",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Conquistador (Tanque Guardia Definitiva)",
            option2 = listOf("Conquistador", "Brutal", "Leyenda: Tenacidad", "Último Esfuerzo", "Fuerzas Renovadas")
        ),
        "wukong" to ChampionRunePair(
            opt1Title = "Conquistador & Asesino / Duelista (Jungla)",
            option1 = listOf("Conquistador", "Fervor de combate", "Golpe de gracia", "Leyenda: Linaje", "Impacto súbito"),
            opt2Title = "Agarre del perpetuo & Tanque (Jungla)",
            option2 = listOf("Agarre del perpetuo", "Inquebrantable", "Coraza ósea", "Crecimiento excesivo", "Impacto súbito")
        ),
        "nunu" to ChampionRunePair(
            opt1Title = "Irrupción de Fase (Bola de Nieve)",
            option1 = listOf("Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Banda de Maná", "Fuente de Vida"),
            opt2Title = "Soberano Gélido (Tanque / CC)",
            option2 = listOf("Soberano Gélido", "Fuente de Vida", "Fuerzas Renovadas", "Sobrecrecimiento", "Trascendencia")
        ),
        "rammus" to ChampionRunePair(
            opt1Title = "Garras del Inmortal & Posición Defensiva",
            option1 = listOf("Garras del Inmortal", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Inquebrantable"),
            opt2Title = "Soberano Gélido (Provocación & Slow)",
            option2 = listOf("Soberano Gélido", "Fuente de Vida", "Fuerzas Renovadas", "Inquebrantable", "Celeridad")
        ),
        "warwick" to ChampionRunePair(
            opt1Title = "Compás Letal & Sed de Sangre (Meta)",
            option1 = listOf("Compás Letal", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Garras del Inmortal (Mordisco H1 Sustain)",
            option2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo")
        ),
        "graves" to ChampionRunePair(
            opt1Title = "Compás Letal & Disparo de Escopeta",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"),
            opt2Title = "Primer Golpe (Burst de Humo & Definitiva)",
            option2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo")
        ),
        "lillia" to ChampionRunePair(
            opt1Title = "Conquistador AP & Polvo de Sueños",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Golpe de Gracia", "Celeridad"),
            opt2Title = "Irrupción de Fase (Velocidad de Salto)",
            option2 = listOf("Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Trascendencia", "Triunfo")
        ),
        "diana" to ChampionRunePair(
            opt1Title = "Conquistador (Luna Creciente AP)",
            option1 = listOf("Conquistador", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos"),
            opt2Title = "Electrocutar (One-Shot H1+H3+Definitiva)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
        ),
        "fiddlesticks" to ChampionRunePair(
            opt1Title = "Primer Golpe & Tormenta de Cuervos",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Trascendencia"),
            opt2Title = "Electrocutar (Burst Emboscada)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Cazador Ingenioso")
        ),

        // --- SUPPORT ---
        "thresh" to ChampionRunePair(
            opt1Title = "Soberano Gélido & Sentencia H1 (Meta)",
            option1 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"),
            opt2Title = "Guardián (Protección Linterna H2)",
            option2 = listOf("Guardián", "Fuente de Vida", "Perseverancia", "Inquebrantable", "Celeridad")
        ),
        "lulu" to ChampionRunePair(
            opt1Title = "Aery (Pix & Enuresis Escudos)",
            option1 = listOf("Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"),
            opt2Title = "Guardián (Anti-Asesinos)",
            option2 = listOf("Guardián", "Fuente de Vida", "Revitalizar", "Sobrecrecimiento", "Banda de Maná")
        ),
        "nami" to ChampionRunePair(
            opt1Title = "Electrocutar (Oleada H3 + H2 Agresiva)",
            option1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"),
            opt2Title = "Aery (Curación & Burbuja H1)",
            option2 = listOf("Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar")
        ),
        "nautilus" to ChampionRunePair(
            opt1Title = "Soberano Gélido (Ancla & Inmovilización)",
            option1 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"),
            opt2Title = "Garras del Inmortal (Resistencia)",
            option2 = listOf("Garras del Inmortal", "Fuente de Vida", "Fuerzas Renovadas", "Inquebrantable", "Perseverancia")
        ),
        "leona" to ChampionRunePair(
            opt1Title = "Soberano Gélido & Eclipse Solar (Meta)",
            option1 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"),
            opt2Title = "Guardián (Peel Defensivo)",
            option2 = listOf("Guardián", "Fuente de Vida", "Perseverancia", "Inquebrantable", "Celeridad")
        ),
        "pyke" to ChampionRunePair(
            opt1Title = "Primer Golpe & Ejecución Definitiva (Meta)",
            option1 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"),
            opt2Title = "Electrocutar (Burst H1 + H3)",
            option2 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Cazador Incesante")
        ),
        "yuumi" to ChampionRunePair(
            opt1Title = "Aery & ¡Acelera! H3 (Meta)",
            option1 = listOf("Aery", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Revitalizar"),
            opt2Title = "Cometa Arcano (Hostigamiento H1)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar")
        ),
        "soraka" to ChampionRunePair(
            opt1Title = "Aery & Infusión Astral H2 (Meta)",
            option1 = listOf("Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"),
            opt2Title = "Guardián (Rescate Aliado)",
            option2 = listOf("Guardián", "Fuente de Vida", "Revitalizar", "Fuerzas Renovadas", "Banda de Maná")
        ),
        "janna" to ChampionRunePair(
            opt1Title = "Aery & Ojo de la Tormenta (Meta)",
            option1 = listOf("Aery", "Banda de Maná", "Trascendencia", "Celeridad", "Revitalizar"),
            opt2Title = "Cometa Arcano (Tornados H1 Poke)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Celeridad")
        ),
        "sona" to ChampionRunePair(
            opt1Title = "Aery & Himno del Valor (Meta)",
            option1 = listOf("Aery", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Revitalizar"),
            opt2Title = "Guardián (Crescendo Definitiva Seguro)",
            option2 = listOf("Guardián", "Fuente de Vida", "Revitalizar", "Sobrecrecimiento", "Banda de Maná")
        ),
        "braum" to ChampionRunePair(
            opt1Title = "Guardián & Detrás de Mí H3 (Meta)",
            option1 = listOf("Guardián", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Perseverancia"),
            opt2Title = "Garras del Inmortal (Golpe Conmocionante)",
            option2 = listOf("Garras del Inmortal", "Fuente de Vida", "Fuerzas Renovadas", "Inquebrantable", "Triunfo")
        ),
        "blitzcrank" to ChampionRunePair(
            opt1Title = "Soberano Gélido & Gancho H1 (Meta)",
            option1 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"),
            opt2Title = "Irrupción de Fase (Velocidad de Escape H2)",
            option2 = listOf("Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Banda de Maná", "Hextello")
        ),
        "karma" to ChampionRunePair(
            opt1Title = "Cometa Arcano & Llama Alma H1 (Meta)",
            option1 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"),
            opt2Title = "Aery (Escudos Mantra H3)",
            option2 = listOf("Aery", "Banda de Maná", "Trascendencia", "Celeridad", "Revitalizar")
        ),
        "rakan" to ChampionRunePair(
            opt1Title = "Soberano Gélido & Gran Entrada (Meta)",
            option1 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"),
            opt2Title = "Guardián (Danza de Batalla H3)",
            option2 = listOf("Guardián", "Fuente de Vida", "Revitalizar", "Perseverancia", "Celeridad")
        ),
        "senna" to ChampionRunePair(
            opt1Title = "Compás Letal & Niebla de Almas (Meta)",
            option1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Fuerzas Renovadas"),
            opt2Title = "Pies Veloces (Kiteo & Curación)",
            option2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Derribado", "Revestimiento de Huesos")
        ),
        "seraphine" to ChampionRunePair(
            opt1Title = "Aery & Eco Acústico (Meta)",
            option1 = listOf("Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"),
            opt2Title = "Cometa Arcano (Poke de Alta Nota)",
            option2 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Colección de Globos Oculares")
        ),
        "morgana" to ChampionRunePair(
            opt1Title = "Cometa Arcano & Sombra Atormentada",
            option1 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta"),
            opt2Title = "Soberano Gélido (Hechizo Oscuro H1 + Definitiva)",
            option2 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Trascendencia")
        ),
        "alistar" to ChampionRunePair(
            opt1Title = "Soberano Gélido & Pulverización (Meta)",
            option1 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"),
            opt2Title = "Guardián (Protección Defensiva)",
            option2 = listOf("Guardián", "Fuente de Vida", "Perseverancia", "Inquebrantable", "Celeridad")
        )
    )

    /**
     * Resuelve las runas oficiales según campeón y rol, asegurando que todos los nombres
     * existan estrictamente en el catálogo oficial de WildRiftSpellsAndRunes.runes.
     */
    fun resolveRunes(
        champ: Champion,
        role: LaneRole
    ): Pair<List<String>, List<String>> {
        val specific = championSpecificRunes[champ.id.lowercase().trim()]
        if (specific != null) {
            return Pair(specific.option1, specific.option2)
        }

        // Fallback arquetípico inteligente si el campeón es nuevo o no está en el mapa estático:
        val opt1: List<String>
        val opt2: List<String>

        when {
            // Asesino / Mago Burst
            champ.damageType == DamageType.MAGIC && !champ.isFrontline && !champ.isRanged -> {
                opt1 = listOf("Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
                opt2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Cazador Ingenioso", "Trascendencia")
            }
            // Mago Control / Rango
            champ.damageType == DamageType.MAGIC && champ.isRanged && role != LaneRole.SUPPORT -> {
                opt1 = listOf("Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Se Avecina Tormenta")
                opt2 = listOf("Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná")
            }
            // Soporte Encantador
            role == LaneRole.SUPPORT && champ.damageType == DamageType.MAGIC -> {
                opt1 = listOf("Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar")
                opt2 = listOf("Guardián", "Fuente de Vida", "Revitalizar", "Sobrecrecimiento", "Banda de Maná")
            }
            // Soporte Tanque / Iniciador
            role == LaneRole.SUPPORT && champ.isFrontline -> {
                opt1 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello")
                opt2 = listOf("Guardián", "Fuente de Vida", "Perseverancia", "Inquebrantable", "Celeridad")
            }
            // Tirador / ADC
            champ.isRanged && champ.damageType == DamageType.PHYSICAL -> {
                opt1 = listOf("Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Sobrecrecimiento")
                opt2 = listOf("Pies Veloces", "Triunfo", "Leyenda: Linaje", "Derribado", "Revestimiento de Huesos")
            }
            // Tanque Frontline
            champ.isFrontline -> {
                opt1 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo")
                opt2 = listOf("Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Inquebrantable", "Trascendencia")
            }
            // Luchador AD / Bruiser
            else -> {
                opt1 = listOf("Conquistador", "Triunfo", "Leyenda: Tenacidad", "Último Esfuerzo", "Revestimiento de Huesos")
                opt2 = listOf("Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Brutal")
            }
        }

        return Pair(opt1, opt2)
    }
}
