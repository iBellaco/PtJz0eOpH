package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Temas de Coach inspirados en las regiones del Universo de Runaterra.
 * No representan temas oficiales ni una autorización de Riot Games.
 * (https://universe.leagueoflegends.com/es_ES/regions/)
 *
 * Piltover es el tema por defecto.
 */
enum class AppTheme(
    val id: String,
    val titleKey: String,
    val regionTag: String,
    val descKey: String,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBorder: Color,
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val primaryGlow: Color,
    val secondary: Color,
    val secondaryLight: Color,
    val secondaryDark: Color,
    val secondaryGlow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean,
    /** True for themes that are independent from Runeterra's region catalogue. */
    val isApplicationTheme: Boolean = false
) {
    // 1. Piltover (Por defecto) - La Ciudad del Progreso: Oro Hextech, azul zafiro, blanco mármol y cian arcano
    PILTOVER(
        id = "PILTOVER",
        titleKey = "Piltover",
        regionTag = "Ciudad del Progreso",
        descKey = "La vanguardia del progreso, tecnología Hextech, oro pulido, zafiro brillante y cian arcano.",
        background = Color(0xFF040A14),
        surface = Color(0xFF0A1626),
        surfaceVariant = Color(0xFF10223B),
        cardBorder = Color(0xFF1F3D64),
        primary = Color(0xFF0AC8B9),
        primaryLight = Color(0xFF00F2FE),
        primaryDark = Color(0xFF005A82),
        primaryGlow = Color(0xFF1D8CF8),
        secondary = Color(0xFFC8AA6E),
        secondaryLight = Color(0xFFF0E6D2),
        secondaryDark = Color(0xFF785A28),
        secondaryGlow = Color(0xFFFFD700),
        textPrimary = Color(0xFFF0E6D2),
        textSecondary = Color(0xFF94A3B8),
        textMuted = Color(0xFF64748B),
        isDark = true
    ),

    // 2. Aguas Estancadas (Bilgewater) - Puerto de Forajidos: Fuego de Cañón Ámbar Pirata y Marea Profunda
    AGUAS_ESTANCADAS(
        id = "AGUAS_ESTANCADAS",
        titleKey = "Aguas Estancadas",
        regionTag = "Puerto de Forajidos",
        descKey = "Bahía de piratas, cazadores de monstruos marinos, bronce salado, pólvora ámbar y marea profunda.",
        background = Color(0xFF0D0B08),
        surface = Color(0xFF201812),
        surfaceVariant = Color(0xFF2D2118),
        cardBorder = Color(0xFF765A3F),
        primary = Color(0xFFD98535),
        primaryLight = Color(0xFFE3A15E),
        primaryDark = Color(0xFF8A4E2B),
        primaryGlow = Color(0xFFE8B47A),
        secondary = Color(0xFF4E9EAC),
        secondaryLight = Color(0xFF79BBC4),
        secondaryDark = Color(0xFF356F7A),
        secondaryGlow = Color(0xFFA2D1D4),
        textPrimary = Color(0xFFFFF3E0),
        textSecondary = Color(0xFFD5A06A),
        textMuted = Color(0xFF8D6E63),
        isDark = true
    ),

    // 3. Ciudad de Bandle - Reino Yordle místico: Lavanda feérica y Esmeralda mágica encantada
    CIUDAD_DE_BANDLE(
        id = "CIUDAD_DE_BANDLE",
        titleKey = "Ciudad de Bandle",
        regionTag = "Hogar de los Yordles",
        descKey = "Reino feérico atemporal repleto de colores mágicos, lavanda encantada, oro solar y hojas brillantes.",
        background = Color(0xFF0D0912),
        surface = Color(0xFF211B2B),
        surfaceVariant = Color(0xFF2B2140),
        cardBorder = Color(0xFF665483),
        primary = Color(0xFF9D86C2),
        primaryLight = Color(0xFFC0B2D8),
        primaryDark = Color(0xFF65507E),
        primaryGlow = Color(0xFFB7A2D2),
        secondary = Color(0xFFC37E9F),
        secondaryLight = Color(0xFFE2B2C7),
        secondaryDark = Color(0xFF7E4F68),
        secondaryGlow = Color(0xFFD9A7BE),
        textPrimary = Color(0xFFF9F5FF),
        textSecondary = Color(0xFFC2AFCB),
        textMuted = Color(0xFF8870A0),
        isDark = true
    ),

    // 4. Demacia - Reino de Honor y Petricita: Azul Real Justiciero y Oro de las Alas de la Justicia
    DEMACIA(
        id = "DEMACIA",
        titleKey = "Demacia",
        regionTag = "Reino de la Justicia",
        descKey = "Petricita resplandeciente, honor inquebrantable, azul real justiciero y oro protector.",
        background = Color(0xFF050A12),
        surface = Color(0xFF111D32),
        surfaceVariant = Color(0xFF172A47),
        cardBorder = Color(0xFF41618D),
        primary = Color(0xFF527EBB),
        primaryLight = Color(0xFF86A9D3),
        primaryDark = Color(0xFF345987),
        primaryGlow = Color(0xFF789BCA),
        secondary = Color(0xFFC7B957),
        secondaryLight = Color(0xFFDDD68B),
        secondaryDark = Color(0xFF81782F),
        secondaryGlow = Color(0xFFD8D28E),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFFA9BFDC),
        textMuted = Color(0xFF64748B),
        isDark = true
    ),

    // 5. El Vacío (The Void) - Dimensión corruptora: Magenta corruptor y Bioluminiscencia Neón Cian
    EL_VACIO(
        id = "EL_VACIO",
        titleKey = "El Vacío",
        regionTag = "La Nada Hambrienta",
        descKey = "Terror cósmico insaciable, púrpura bioluminiscente, magenta alienígena y oscuridad corruptora.",
        background = Color(0xFF09070C),
        surface = Color(0xFF1C1524),
        surfaceVariant = Color(0xFF251B35),
        cardBorder = Color(0xFF674D78),
        primary = Color(0xFFA15CA8),
        primaryLight = Color(0xFFC98ACB),
        primaryDark = Color(0xFF6B3E70),
        primaryGlow = Color(0xFFB77BB9),
        secondary = Color(0xFF7665AF),
        secondaryLight = Color(0xFFA29ACB),
        secondaryDark = Color(0xFF4F4478),
        secondaryGlow = Color(0xFF9288BF),
        textPrimary = Color(0xFFFCE7F3),
        textSecondary = Color(0xFF9878A1),
        textMuted = Color(0xFF7E22CE),
        isDark = true
    ),

    // 6. Freljord - Tierras del Hielo Puro: Cian glacial congelado y Fuego Ancestral de la Forja de Ornn
    FRELJORD(
        id = "FRELJORD",
        titleKey = "Freljord",
        regionTag = "Tierras del Norte",
        descKey = "Hielo puro ancestral, escarcha ártica implacable, azul glacial y templanza de los Hijos del Hielo.",
        background = Color(0xFF050A10),
        surface = Color(0xFF101E2C),
        surfaceVariant = Color(0xFF183047),
        cardBorder = Color(0xFF416A88),
        primary = Color(0xFF55AAB8),
        primaryLight = Color(0xFF9BCDD3),
        primaryDark = Color(0xFF3A7882),
        primaryGlow = Color(0xFF80C0C7),
        secondary = Color(0xFF7B9DBE),
        secondaryLight = Color(0xFFB4C8DB),
        secondaryDark = Color(0xFF526C89),
        secondaryGlow = Color(0xFF9DB4CB),
        textPrimary = Color(0xFFF0F9FF),
        textSecondary = Color(0xFFA0B9CF),
        textMuted = Color(0xFF4B6B94),
        isDark = true
    ),

    // 7. Islas de la Sombra - Niebla Negra y Almas Malditas: Jade espectral y Púrpura de Niebla Negra
    ISLAS_DE_LA_SOMBRA(
        id = "ISLAS_DE_LA_SOMBRA",
        titleKey = "Islas de la Sombra",
        regionTag = "Niebla Negra",
        descKey = "Tierras consumidas por la Ruina, jade espectral fulgurante, almas atormentadas y frío de ultratumba.",
        background = Color(0xFF020A09),
        surface = Color(0xFF0C211E),
        surfaceVariant = Color(0xFF14302E),
        cardBorder = Color(0xFF39756E),
        primary = Color(0xFF4FAD91),
        primaryLight = Color(0xFF94C9B7),
        primaryDark = Color(0xFF34725F),
        primaryGlow = Color(0xFF76BAA3),
        secondary = Color(0xFF4B9C93),
        secondaryLight = Color(0xFF99CDC1),
        secondaryDark = Color(0xFF326B65),
        secondaryGlow = Color(0xFF7EBBB1),
        textPrimary = Color(0xFFE6FFFA),
        textSecondary = Color(0xFF9BCBC1),
        textMuted = Color(0xFF115E59),
        isDark = true
    ),

    // 8. Ixtal - Selva Elemental Ancestral: Esmeralda axiomática y Turquesa Elemental de Ríos
    IXTAL(
        id = "IXTAL",
        titleKey = "Ixtal",
        regionTag = "Magia Elemental",
        descKey = "Dominio de los axiomas elementales, selva densa esmeralda, orquídeas salvajes y bronce arcano.",
        background = Color(0xFF050B07),
        surface = Color(0xFF14291E),
        surfaceVariant = Color(0xFF183725),
        cardBorder = Color(0xFF467B5D),
        primary = Color(0xFF4C9F78),
        primaryLight = Color(0xFF8BC8A9),
        primaryDark = Color(0xFF326C54),
        primaryGlow = Color(0xFF75B998),
        secondary = Color(0xFFB28C4B),
        secondaryLight = Color(0xFFD4BC7E),
        secondaryDark = Color(0xFF765E32),
        secondaryGlow = Color(0xFFC8AB68),
        textPrimary = Color(0xFFECFDF5),
        textSecondary = Color(0xFF9CC8AF),
        textMuted = Color(0xFF4B8B67),
        isDark = true
    ),

    // 9. Jonia - Las Tierras Primigenias: Rosa Flor de Loto y Verde Jade Espiritual de Armonía
    JONIA(
        id = "JONIA",
        titleKey = "Jonia",
        regionTag = "Tierras Primigenias",
        descKey = "Equilibrio supremo de la naturaleza, flores de loto sagradas, magia espiritual y cerezos en flor.",
        background = Color(0xFF0D090E),
        surface = Color(0xFF241C25),
        surfaceVariant = Color(0xFF2A1F2D),
        cardBorder = Color(0xFF72556F),
        primary = Color(0xFFC184A5),
        primaryLight = Color(0xFFDFB5C9),
        primaryDark = Color(0xFF80556D),
        primaryGlow = Color(0xFFD49BB6),
        secondary = Color(0xFF5D9AB5),
        secondaryLight = Color(0xFFA2C8D7),
        secondaryDark = Color(0xFF3F687C),
        secondaryGlow = Color(0xFF88B6C8),
        textPrimary = Color(0xFFFDF2F8),
        textSecondary = Color(0xFFC895AD),
        textMuted = Color(0xFF834E79),
        isDark = true
    ),

    // 10. Noxus - Imperio Conquistador: Rojo Carmesí de Sangre y Acero Forjado Oscuro
    NOXUS(
        id = "NOXUS",
        titleKey = "Noxus",
        regionTag = "Imperio de la Fuerza",
        descKey = "Fuerza militar implacable, rojo sangre de conquista, acero forjado y triunfo de los fuertes.",
        background = Color(0xFF0C0708),
        surface = Color(0xFF241719),
        surfaceVariant = Color(0xFF2A171B),
        cardBorder = Color(0xFF75484D),
        primary = Color(0xFFB95E63),
        primaryLight = Color(0xFFDA8C8F),
        primaryDark = Color(0xFF773D41),
        primaryGlow = Color(0xFFC97B7F),
        secondary = Color(0xFFC47B50),
        secondaryLight = Color(0xFFDFAF8E),
        secondaryDark = Color(0xFF815234),
        secondaryGlow = Color(0xFFD39A77),
        textPrimary = Color(0xFFFCE7E9),
        textSecondary = Color(0xFFC48B92),
        textMuted = Color(0xFF8A5C63),
        isDark = true
    ),

    // 11. Shurima - El Imperio del Desierto: Oro del Disco Solar y Lapislázuli/Turquesa de Oasis Imperial
    SHURIMA(
        id = "SHURIMA",
        titleKey = "Shurima",
        regionTag = "Imperio del Disco Solar",
        descKey = "El gran Disco Solar de los Ascendidos, arenas doradas, ámbar ardiente y gloria renacida.",
        background = Color(0xFF0E0A05),
        surface = Color(0xFF2B2316),
        surfaceVariant = Color(0xFF302713),
        cardBorder = Color(0xFF7E6538),
        primary = Color(0xFFC79743),
        primaryLight = Color(0xFFDBB56D),
        primaryDark = Color(0xFF86632C),
        primaryGlow = Color(0xFFD4A962),
        secondary = Color(0xFF55A89D),
        secondaryLight = Color(0xFFA3D1C9),
        secondaryDark = Color(0xFF39736C),
        secondaryGlow = Color(0xFF83C4B9),
        textPrimary = Color(0xFFFFF7ED),
        textSecondary = Color(0xFFD2A970),
        textMuted = Color(0xFF8A6538),
        isDark = true
    ),

    // 12. Targon - La Cumbre Celestial: Violeta Cósmico y Oro Solar Divino de los Solari
    TARGON(
        id = "TARGON",
        titleKey = "Targon",
        regionTag = "Cumbre Celestial",
        descKey = "Morada de los Aspectos Celestiales, violeta estelar profundo, plata lunar y fuego solar divino.",
        background = Color(0xFF08060F),
        surface = Color(0xFF201B31),
        surfaceVariant = Color(0xFF241D42),
        cardBorder = Color(0xFF5D4C88),
        primary = Color(0xFF846EBA),
        primaryLight = Color(0xFFB0A1D1),
        primaryDark = Color(0xFF584A7D),
        primaryGlow = Color(0xFF9D8BC8),
        secondary = Color(0xFFC4A35B),
        secondaryLight = Color(0xFFDFCB91),
        secondaryDark = Color(0xFF826C3B),
        secondaryGlow = Color(0xFFD4BC77),
        textPrimary = Color(0xFFF3E5F5),
        textSecondary = Color(0xFFA594C9),
        textMuted = Color(0xFF6E5D91),
        isDark = true
    ),
    ZAUN(
        id = "ZAUN",
        titleKey = "Zaun",
        regionTag = "Subciudad Quimtech",
        descKey = "Ingenio desenfrenado en las profundidades, verde ácido químico, humo Zaun y resplandor Shimmer.",
        background = Color(0xFF050A07), surface = Color(0xFF13281C),
        surfaceVariant = Color(0xFF183421), cardBorder = Color(0xFF477B56),
        primary = Color(0xFF6EAD48), primaryLight = Color(0xFFA5CE85),
        primaryDark = Color(0xFF487330), primaryGlow = Color(0xFF8EBB6D),
        secondary = Color(0xFFB19B49), secondaryLight = Color(0xFFD1C582),
        secondaryDark = Color(0xFF75672F), secondaryGlow = Color(0xFFC2B46C),
        textPrimary = Color(0xFFE8FDF0), textSecondary = Color(0xFF9FC18E),
        textMuted = Color(0xFF5F8151), isDark = true
    ),

    // Application themes are deliberately independent from region lore. Each
    // palette has its own contrast scale so its text remains readable without
    // borrowing a region's visual identity.
    WHITE(
        id = "WHITE",
        titleKey = "White",
        regionTag = "Tema de aplicación",
        descKey = "Fondo luminoso con texto carbón y acentos cian para leer sin esfuerzo.",
        background = Color(0xFFF4F6FA),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFE9EFF7),
        cardBorder = Color(0xFFC7D2E1),
        primary = Color(0xFF087FAE),
        primaryLight = Color(0xFF0B5E7A),
        primaryDark = Color(0xFF05506E),
        primaryGlow = Color(0xFF34B5D5),
        secondary = Color(0xFFA97609),
        secondaryLight = Color(0xFFC8941F),
        secondaryDark = Color(0xFF7A5500),
        secondaryGlow = Color(0xFFDAB55E),
        textPrimary = Color(0xFF152033),
        textSecondary = Color(0xFF4E5E74),
        textMuted = Color(0xFF6B7B91),
        isDark = false,
        isApplicationTheme = true
    ),
    BLACK(
        id = "BLACK",
        titleKey = "Black",
        regionTag = "Tema de aplicación",
        descKey = "Negro OLED monocromático con jerarquía clara para leer de noche.",
        background = Color(0xFF050505),
        surface = Color(0xFF111111),
        surfaceVariant = Color(0xFF1D1D1D),
        cardBorder = Color(0xFF3D3D3D),
        primary = Color(0xFFF2F2F2),
        primaryLight = Color(0xFFFFFFFF),
        primaryDark = Color(0xFFA6A6A6),
        primaryGlow = Color(0xFFFFFFFF),
        secondary = Color(0xFF949494),
        secondaryLight = Color(0xFFD4D4D4),
        secondaryDark = Color(0xFF5C5C5C),
        secondaryGlow = Color(0xFFE8E8E8),
        textPrimary = Color(0xFFF8F8F8),
        textSecondary = Color(0xFFC1C1C1),
        textMuted = Color(0xFF858585),
        isDark = true,
        isApplicationTheme = true
    ),
    AURORA(
        id = "AURORA",
        titleKey = "Aurora",
        regionTag = "Tema de aplicación",
        descKey = "Violeta eléctrico y menta suave para distinguir estados sin saturar la pantalla.",
        background = Color(0xFF110D20),
        surface = Color(0xFF21183A),
        surfaceVariant = Color(0xFF302650),
        cardBorder = Color(0xFF624A87),
        primary = Color(0xFFF07CFF),
        primaryLight = Color(0xFFF7B7FF),
        primaryDark = Color(0xFF9B3AAE),
        primaryGlow = Color(0xFFFF9CFF),
        secondary = Color(0xFF53E6BA),
        secondaryLight = Color(0xFFA1F4D8),
        secondaryDark = Color(0xFF268D73),
        secondaryGlow = Color(0xFF85F2CE),
        textPrimary = Color(0xFFFFF5FF),
        textSecondary = Color(0xFFD8C8E8),
        textMuted = Color(0xFFA793BE),
        isDark = true,
        isApplicationTheme = true
    ),
    EMBER(
        id = "EMBER",
        titleKey = "Ember",
        regionTag = "Tema de aplicación",
        descKey = "Carbón granate con coral y azul hielo para un contraste cálido-frío.",
        background = Color(0xFF161114),
        surface = Color(0xFF292027),
        surfaceVariant = Color(0xFF3B2D35),
        cardBorder = Color(0xFF694B59),
        primary = Color(0xFFFF6B6B),
        primaryLight = Color(0xFFFFA0A0),
        primaryDark = Color(0xFFB83E4A),
        primaryGlow = Color(0xFFFF8F8F),
        secondary = Color(0xFF8ED1FC),
        secondaryLight = Color(0xFFC8EAFF),
        secondaryDark = Color(0xFF477B9D),
        secondaryGlow = Color(0xFFB0E2FF),
        textPrimary = Color(0xFFFFF7F7),
        textSecondary = Color(0xFFE5C9D0),
        textMuted = Color(0xFFB48E9A),
        isDark = true,
        isApplicationTheme = true
    ),
    SAKURA(
        id = "SAKURA",
        titleKey = "Sakura",
        regionTag = "Tema de aplicación",
        descKey = "Ciruela profunda con lavanda y jade pálido para una lectura suave.",
        background = Color(0xFF17131B),
        surface = Color(0xFF29222F),
        surfaceVariant = Color(0xFF3A3043),
        cardBorder = Color(0xFF695878),
        primary = Color(0xFFE6B0FF),
        primaryLight = Color(0xFFF2D5FF),
        primaryDark = Color(0xFF996FBA),
        primaryGlow = Color(0xFFF0C8FF),
        secondary = Color(0xFF8FF3E1),
        secondaryLight = Color(0xFFC4FFF5),
        secondaryDark = Color(0xFF4BA898),
        secondaryGlow = Color(0xFFB6FAEE),
        textPrimary = Color(0xFFFFF8FF),
        textSecondary = Color(0xFFD9CDE2),
        textMuted = Color(0xFFA694B0),
        isDark = true,
        isApplicationTheme = true
    ),
    FOREST(
        id = "FOREST",
        titleKey = "Forest",
        regionTag = "Tema de aplicación",
        descKey = "Verde pino, lima apagada y arcilla para una interfaz serena y orgánica.",
        background = Color(0xFF10140F),
        surface = Color(0xFF1E2519),
        surfaceVariant = Color(0xFF303922),
        cardBorder = Color(0xFF596443),
        primary = Color(0xFFC5E478),
        primaryLight = Color(0xFFE2F4AD),
        primaryDark = Color(0xFF7E9C3D),
        primaryGlow = Color(0xFFD8F29A),
        secondary = Color(0xFFD9A48F),
        secondaryLight = Color(0xFFF0C5B5),
        secondaryDark = Color(0xFF98624F),
        secondaryGlow = Color(0xFFE8B6A1),
        textPrimary = Color(0xFFF5FAEE),
        textSecondary = Color(0xFFD0D8C1),
        textMuted = Color(0xFF9BA78A),
        isDark = true,
        isApplicationTheme = true
    ),
    COBALT(
        id = "COBALT",
        titleKey = "Cobalt",
        regionTag = "Tema de aplicación",
        descKey = "Pizarra azulada con ámbar y perla para separar datos sin usar oro regional.",
        background = Color(0xFF0D1116),
        surface = Color(0xFF1A232E),
        surfaceVariant = Color(0xFF293744),
        cardBorder = Color(0xFF4E6478),
        primary = Color(0xFFFFB86B),
        primaryLight = Color(0xFFFFD09B),
        primaryDark = Color(0xFFB97835),
        primaryGlow = Color(0xFFFFC98A),
        secondary = Color(0xFFA7B5FF),
        secondaryLight = Color(0xFFD1D8FF),
        secondaryDark = Color(0xFF6573C2),
        secondaryGlow = Color(0xFFBCC7FF),
        textPrimary = Color(0xFFF6F8FB),
        textSecondary = Color(0xFFC1CCD7),
        textMuted = Color(0xFF8B9AA8),
        isDark = true,
        isApplicationTheme = true
    ),
    GRAPHITE(
        id = "GRAPHITE",
        titleKey = "Graphite",
        regionTag = "Tema de aplicación",
        descKey = "Pizarra neutra con cobre suave para una interfaz sobria y precisa.",
        background = Color(0xFF121416),
        surface = Color(0xFF252A2E),
        surfaceVariant = Color(0xFF343B40),
        cardBorder = Color(0xFF59636A),
        primary = Color(0xFFD7E0E5),
        primaryLight = Color(0xFFF3F7F9),
        primaryDark = Color(0xFF89969D),
        primaryGlow = Color(0xFFE8F0F3),
        secondary = Color(0xFFD68C63),
        secondaryLight = Color(0xFFF0B394),
        secondaryDark = Color(0xFF915638),
        secondaryGlow = Color(0xFFE8A47D),
        textPrimary = Color(0xFFF4F6F7),
        textSecondary = Color(0xFFC5CDD1),
        textMuted = Color(0xFF939DA2),
        isDark = true,
        isApplicationTheme = true
    ),
    MINT(
        id = "MINT",
        titleKey = "Mint",
        regionTag = "Tema de aplicación",
        descKey = "Menta mineral y lavanda para una lectura fresca con señales suaves.",
        background = Color(0xFF081716),
        surface = Color(0xFF112B29),
        surfaceVariant = Color(0xFF1D403D),
        cardBorder = Color(0xFF39706A),
        primary = Color(0xFF8AF0D0),
        primaryLight = Color(0xFFC0FFE9),
        primaryDark = Color(0xFF4BA58D),
        primaryGlow = Color(0xFFA8F8DE),
        secondary = Color(0xFFD1A6FF),
        secondaryLight = Color(0xFFE8D4FF),
        secondaryDark = Color(0xFF8662B3),
        secondaryGlow = Color(0xFFDEBDFF),
        textPrimary = Color(0xFFF2FFFC),
        textSecondary = Color(0xFFC5E4DE),
        textMuted = Color(0xFF8BB8B0),
        isDark = true,
        isApplicationTheme = true
    ),
    VOLT(
        id = "VOLT",
        titleKey = "Volt",
        regionTag = "Tema de aplicación",
        descKey = "Lima ácida y coral sobre carbón para detectar cambios al instante.",
        background = Color(0xFF13120A),
        surface = Color(0xFF272314),
        surfaceVariant = Color(0xFF3C351D),
        cardBorder = Color(0xFF6C5C2C),
        primary = Color(0xFFD8FF62),
        primaryLight = Color(0xFFEBFFAE),
        primaryDark = Color(0xFF8FAF2D),
        primaryGlow = Color(0xFFE4FF87),
        secondary = Color(0xFFFF9F68),
        secondaryLight = Color(0xFFFFC29B),
        secondaryDark = Color(0xFFB45D35),
        secondaryGlow = Color(0xFFFFB685),
        textPrimary = Color(0xFFFFFDEB),
        textSecondary = Color(0xFFE0D9B9),
        textMuted = Color(0xFFAAA06F),
        isDark = true,
        isApplicationTheme = true
    ),
    CANDY(
        id = "CANDY",
        titleKey = "Candy",
        regionTag = "Tema de aplicación",
        descKey = "Rosa frambuesa y azul glacial para una interfaz ligera y expresiva.",
        background = Color(0xFF160D1A),
        surface = Color(0xFF29152E),
        surfaceVariant = Color(0xFF402044),
        cardBorder = Color(0xFF754C79),
        primary = Color(0xFFFF8AB4),
        primaryLight = Color(0xFFFFC0D7),
        primaryDark = Color(0xFFB75379),
        primaryGlow = Color(0xFFFFA9C9),
        secondary = Color(0xFF7BE7FF),
        secondaryLight = Color(0xFFBCEFFF),
        secondaryDark = Color(0xFF438DA0),
        secondaryGlow = Color(0xFFA4EDFF),
        textPrimary = Color(0xFFFFF6FC),
        textSecondary = Color(0xFFE2C9DE),
        textMuted = Color(0xFFB18BAE),
        isDark = true,
        isApplicationTheme = true
    ),
    SLATE(
        id = "SLATE",
        titleKey = "Slate",
        regionTag = "Tema de aplicación",
        descKey = "Gris pizarra, plata azulada y rosa tenue para una lectura equilibrada.",
        background = Color(0xFF101319),
        surface = Color(0xFF1D2634),
        surfaceVariant = Color(0xFF2C394A),
        cardBorder = Color(0xFF52647A),
        primary = Color(0xFFC1D0E0),
        primaryLight = Color(0xFFE2EBF4),
        primaryDark = Color(0xFF74869B),
        primaryGlow = Color(0xFFD5E2EF),
        secondary = Color(0xFFF1A7C6),
        secondaryLight = Color(0xFFFFD1E1),
        secondaryDark = Color(0xFFB06A87),
        secondaryGlow = Color(0xFFFFBBD4),
        textPrimary = Color(0xFFF5F8FC),
        textSecondary = Color(0xFFC4CEDA),
        textMuted = Color(0xFF8D9BAC),
        isDark = true,
        isApplicationTheme = true
    );

    companion object {
        // Compatibilidad hacia atrás si un usuario guardó HEXTECH en SharedPrefs
        fun fromId(id: String?): AppTheme {
            if (id == null) return PILTOVER
            if (id == "HEXTECH") return PILTOVER
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: PILTOVER
        }
    }
}

enum class NavBarColorOption(
    val id: String,
    val titleKey: String,
    val descKey: String,
    val colorHex: Color,
    val containerColor: Color,
    val accentColor: Color,
    val isAutomatic: Boolean
) {
    THEME_AUTO(
        id = "THEME_AUTO",
        titleKey = "Automático (Sincronizado con Tema)",
        descKey = "Se adapta automáticamente a la región activa",
        colorHex = Color(0xFF00E5FF),
        containerColor = Color.Unspecified,
        accentColor = Color.Unspecified,
        isAutomatic = true
    ),
    PILTOVER_GOLD(
        id = "PILTOVER_GOLD",
        titleKey = "Piltover Oro",
        descKey = "Elegante oro Hextech refinado",
        colorHex = Color(0xFFC8AA6E),
        containerColor = Color(0xFF0A1626),
        accentColor = Color(0xFFC8AA6E),
        isAutomatic = false
    ),
    PILTOVER_CYAN(
        id = "PILTOVER_CYAN",
        titleKey = "Piltover Cian",
        descKey = "Resplandor cian arcano de cristal",
        colorHex = Color(0xFF0AC8B9),
        containerColor = Color(0xFF04121A),
        accentColor = Color(0xFF0AC8B9),
        isAutomatic = false
    ),
    BILGEWATER_TEAL(
        id = "BILGEWATER_TEAL",
        titleKey = "Aguas Estancadas",
        descKey = "Fuego pirata ámbar y marea profunda",
        colorHex = Color(0xFFFF6F00),
        containerColor = Color(0xFF221107),
        accentColor = Color(0xFFFF6F00),
        isAutomatic = false
    ),
    BANDLE_LAVENDER(
        id = "BANDLE_LAVENDER",
        titleKey = "Bandle Mágico",
        descKey = "Lavanda y magia feérica yordle",
        colorHex = Color(0xFFB388FF),
        containerColor = Color(0xFF201035),
        accentColor = Color(0xFFB388FF),
        isAutomatic = false
    ),
    DEMACIA_ROYAL(
        id = "DEMACIA_ROYAL",
        titleKey = "Demacia Real",
        descKey = "Azul justiciero y nobleza demaciana",
        colorHex = Color(0xFF2563EB),
        containerColor = Color(0xFF081830),
        accentColor = Color(0xFF2563EB),
        isAutomatic = false
    ),
    VOID_MAGENTA(
        id = "VOID_MAGENTA",
        titleKey = "El Vacío",
        descKey = "Púrpura y magenta alienígena",
        colorHex = Color(0xFFD946EF),
        containerColor = Color(0xFF160424),
        accentColor = Color(0xFFD946EF),
        isAutomatic = false
    ),
    FRELJORD_GLACIAL(
        id = "FRELJORD_GLACIAL",
        titleKey = "Freljord Glacial",
        descKey = "Hielo puro y azul escarcha",
        colorHex = Color(0xFF00E5FF),
        containerColor = Color(0xFF071829),
        accentColor = Color(0xFF00E5FF),
        isAutomatic = false
    ),
    SHADOW_ISLES_JADE(
        id = "SHADOW_ISLES_JADE",
        titleKey = "Islas de la Sombra",
        descKey = "Jade espectral y niebla negra",
        colorHex = Color(0xFF00FFB3),
        containerColor = Color(0xFF041818),
        accentColor = Color(0xFF00FFB3),
        isAutomatic = false
    ),
    IXTAL_EMERALD(
        id = "IXTAL_EMERALD",
        titleKey = "Ixtal Selva",
        descKey = "Esmeralda elemental primigenia",
        colorHex = Color(0xFF059669),
        containerColor = Color(0xFF071F11),
        accentColor = Color(0xFF059669),
        isAutomatic = false
    ),
    IONIA_BLOSSOM(
        id = "IONIA_BLOSSOM",
        titleKey = "Jonia Espiritual",
        descKey = "Rosa flor de loto y cerezo",
        colorHex = Color(0xFFF472B6),
        containerColor = Color(0xFF1C0E20),
        accentColor = Color(0xFFF472B6),
        isAutomatic = false
    ),
    NOXUS_CRIMSON(
        id = "NOXUS_CRIMSON",
        titleKey = "Noxus Carmesí",
        descKey = "Rojo sangre y acero bélico",
        colorHex = Color(0xFFFF1744),
        containerColor = Color(0xFF19060A),
        accentColor = Color(0xFFFF1744),
        isAutomatic = false
    ),
    SHURIMA_GOLD(
        id = "SHURIMA_GOLD",
        titleKey = "Shurima Solar",
        descKey = "Oro imperial del Disco Solar",
        colorHex = Color(0xFFFFB300),
        containerColor = Color(0xFF211303),
        accentColor = Color(0xFFFFB300),
        isAutomatic = false
    ),
    TARGON_COSMIC(
        id = "TARGON_COSMIC",
        titleKey = "Targon Cósmico",
        descKey = "Violeta estelar y cumbre celestial",
        colorHex = Color(0xFF7C3AED),
        containerColor = Color(0xFF120C29),
        accentColor = Color(0xFF7C3AED),
        isAutomatic = false
    ),
    ZAUN_CHEMTECH(
        id = "ZAUN_CHEMTECH",
        titleKey = "Zaun Quimtech",
        descKey = "Verde tóxico ácido de Zaun",
        colorHex = Color(0xFF39FF14),
        containerColor = Color(0xFF061A0F),
        accentColor = Color(0xFF39FF14),
        isAutomatic = false
    )
}

/** Estilos visuales de navegación disponibles desde Temas para cuentas Premium. */
enum class CoachNavStyle(val id: String, val title: String) {
    ORIGINAL("original", "Original de Coach"),
    ARC_GLOWING("arc_glowing", "Anillo Hextech"),
    LIQUID("liquid", "Flujo arcano"),
    NEON_WAVE("neon_wave", "Onda de energía"),
    CAPSULE("capsule", "Cápsula Hextech"),
    RUNIC_TILE("runic_tile", "Sello rúnico"),
    TOP_BEACON("top_beacon", "Faro superior"),
    UNDERLINE("underline", "Trazo luminoso"),
    ACTIVE_LABEL("active_label", "Etiqueta activa"),
    ORBIT("orbit", "Órbita de cristal"),
    GLASS_PILL("glass_pill", "Cápsula de vidrio"),
    TABS_V2("tabs_v2", "Pestañas compactas"),
    PURPLE_MESSAGES("purple_messages", "Mensajes violeta"),
    MENISCUS("meniscus", "Menisco luminoso"),
    VIDEO_LIQUID_GLASS("video_liquid_glass", "Vidrio líquido"),
    VIDEO_DARK_NEON_LENS("video_dark_neon_lens", "Lente neón oscuro"),
    VIDEO_BOUNCE("video_bounce", "Rebote activo"),
    VIDEO_NEON_WAVE("video_neon_wave", "Onda neón"),
    VIDEO_SPOTLIGHT("video_spotlight", "Foco de selección"),
    VIDEO_FLOATING_BUTTON("video_floating_button", "Botón flotante"),
    GLASS_WAVE("glass_wave", "Vidrio ondulante"),
    GLASS_CONE("glass_cone", "Vidrio prismático"),
    GLASS_BEAD("glass_bead", "Esfera de vidrio"),
    WARM_TABS("warm_tabs", "Pestañas cálidas"),
    RIBBON_MATRIX("ribbon_matrix", "Cinta modular"),
    PRISM_LATTICE("prism_lattice", "Malla prismática");

    companion object {
        fun fromId(id: String?): CoachNavStyle = entries.firstOrNull { it.id == id } ?: ORIGINAL
    }
}

object AppThemeManager {
    private const val PREFS_KEY_THEME = "selected_app_theme_id"
    private const val PREFS_KEY_NAV_BAR = "selected_app_navbar_id"
    private const val PREFS_KEY_NAV_STYLE = "selected_coach_nav_style"
    private const val PREFS_KEY_OLED_MODE = "selected_app_oled_mode"

    var currentTheme by mutableStateOf(AppTheme.PILTOVER)
        private set

    var currentNavBarOption by mutableStateOf(NavBarColorOption.THEME_AUTO)
        private set

    var currentNavStyle by mutableStateOf(CoachNavStyle.ORIGINAL)
        private set

    var isOledMode by mutableStateOf(false)
        private set

    var isParticlesEnabled by mutableStateOf(false)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("coach_visual_preferences", Context.MODE_PRIVATE)
        if (!prefs.contains(PREFS_KEY_THEME)) {
            val previous = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, "app_prefs_enc")
            prefs.edit().putString(PREFS_KEY_THEME, previous.getString(PREFS_KEY_THEME, AppTheme.PILTOVER.id))
                .putString(PREFS_KEY_NAV_BAR, previous.getString(PREFS_KEY_NAV_BAR, NavBarColorOption.THEME_AUTO.id))
                .putString(PREFS_KEY_NAV_STYLE, previous.getString(PREFS_KEY_NAV_STYLE, CoachNavStyle.ORIGINAL.id))
                .putBoolean(PREFS_KEY_OLED_MODE, previous.getBoolean(PREFS_KEY_OLED_MODE, false))
                .putBoolean("particles_enabled", previous.getBoolean("particles_enabled", false)).apply()
        }
        val savedThemeId = prefs.getString(PREFS_KEY_THEME, AppTheme.PILTOVER.id) ?: AppTheme.PILTOVER.id
        val savedNavId = prefs.getString(PREFS_KEY_NAV_BAR, NavBarColorOption.THEME_AUTO.id) ?: NavBarColorOption.THEME_AUTO.id
        val savedOled = prefs.getBoolean(PREFS_KEY_OLED_MODE, false)
        val savedParticles = prefs.getBoolean("particles_enabled", false)

        currentTheme = AppTheme.fromId(savedThemeId)
        currentNavBarOption = NavBarColorOption.entries.find { it.id == savedNavId } ?: NavBarColorOption.THEME_AUTO
        currentNavStyle = CoachNavStyle.fromId(prefs.getString(PREFS_KEY_NAV_STYLE, CoachNavStyle.ORIGINAL.id))
        isOledMode = savedOled
        isParticlesEnabled = savedParticles
    }

    fun setOledMode(enabled: Boolean, context: Context? = null) {
        isOledMode = enabled
        context?.let {
            val prefs = it.getSharedPreferences("coach_visual_preferences", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREFS_KEY_OLED_MODE, enabled).apply()
        }
    }
    
    fun setParticlesEnabled(enabled: Boolean, context: Context? = null) {
        isParticlesEnabled = enabled
        context?.let {
            val prefs = it.getSharedPreferences("coach_visual_preferences", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("particles_enabled", enabled).apply()
        }
    }

    fun setTheme(theme: AppTheme, context: Context? = null) {
        currentTheme = theme
        context?.let {
            val prefs = it.getSharedPreferences("coach_visual_preferences", Context.MODE_PRIVATE)
            prefs.edit().putString(PREFS_KEY_THEME, theme.id).apply()
        }
    }

    fun setNavBarOption(option: NavBarColorOption, context: Context? = null) {
        currentNavBarOption = option
        context?.let {
            val prefs = it.getSharedPreferences("coach_visual_preferences", Context.MODE_PRIVATE)
            prefs.edit().putString(PREFS_KEY_NAV_BAR, option.id).apply()
        }
    }

    fun setNavStyle(style: CoachNavStyle, context: Context? = null) {
        currentNavStyle = style
        context?.let { it.getSharedPreferences("coach_visual_preferences", Context.MODE_PRIVATE)
            .edit().putString(PREFS_KEY_NAV_STYLE, style.id).apply() }
    }

    fun toggleLightDark(context: Context? = null) {
        // No-op ya que todos los temas son oscuros inmersivos
    }

    fun getNavBarBackgroundColor(): Color {
        return if (currentNavBarOption.isAutomatic) {
            currentTheme.surface.copy(alpha = 0.85f)
        } else {
            currentNavBarOption.containerColor
        }
    }

    fun getNavBarAccentColor(): Color {
        return if (currentNavBarOption.isAutomatic) {
            currentTheme.secondary
        } else {
            currentNavBarOption.accentColor
        }
    }

    fun getNavBarIndicatorColor(): Color {
        return if (currentNavBarOption.isAutomatic) {
            currentTheme.primary.copy(alpha = 0.22f)
        } else {
            currentNavBarOption.colorHex.copy(alpha = 0.22f)
        }
    }

    fun getNavBarSelectedIconColor(): Color {
        return if (currentNavBarOption.isAutomatic) {
            currentTheme.primary
        } else {
            currentNavBarOption.colorHex
        }
    }

    fun getNavBarSelectedTextColor(): Color {
        return if (currentNavBarOption.isAutomatic) {
            currentTheme.secondary
        } else {
            currentNavBarOption.accentColor
        }
    }

    fun getNavBarUnselectedColor(): Color {
        return currentTheme.textMuted.copy(alpha = 0.85f)
    }
}
