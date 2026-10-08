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
    val isDark: Boolean
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
        background = Color(0xFF0F0702),
        surface = Color(0xFF221107),
        surfaceVariant = Color(0xFF361C0D),
        cardBorder = Color(0xFF6E3A19),
        primary = Color(0xFFFF8F00),
        primaryLight = Color(0xFFFFB300),
        primaryDark = Color(0xFFB34700),
        primaryGlow = Color(0xFFFFD54F),
        secondary = Color(0xFF00B4D8),
        secondaryLight = Color(0xFF48CAE4),
        secondaryDark = Color(0xFF0077B6),
        secondaryGlow = Color(0xFF90E0EF),
        textPrimary = Color(0xFFFFF3E0),
        textSecondary = Color(0xFFFFB74D),
        textMuted = Color(0xFF8D6E63),
        isDark = true
    ),

    // 3. Ciudad de Bandle - Reino Yordle místico: Lavanda feérica y Esmeralda mágica encantada
    CIUDAD_DE_BANDLE(
        id = "CIUDAD_DE_BANDLE",
        titleKey = "Ciudad de Bandle",
        regionTag = "Hogar de los Yordles",
        descKey = "Reino feérico atemporal repleto de colores mágicos, lavanda encantada, oro solar y hojas brillantes.",
        background = Color(0xFF0F0619),
        surface = Color(0xFF201035),
        surfaceVariant = Color(0xFF321A50),
        cardBorder = Color(0xFF5A2F8E),
        primary = Color(0xFFB388FF),
        primaryLight = Color(0xFFD1B3FF),
        primaryDark = Color(0xFF5E2D91),
        primaryGlow = Color(0xFFE040FB),
        secondary = Color(0xFFF472B6),
        secondaryLight = Color(0xFFFBCFE8),
        secondaryDark = Color(0xFFBE185D),
        secondaryGlow = Color(0xFFF9A8D4),
        textPrimary = Color(0xFFF9F5FF),
        textSecondary = Color(0xFFC3AED6),
        textMuted = Color(0xFF8870A0),
        isDark = true
    ),

    // 4. Demacia - Reino de Honor y Petricita: Azul Real Justiciero y Oro de las Alas de la Justicia
    DEMACIA(
        id = "DEMACIA",
        titleKey = "Demacia",
        regionTag = "Reino de la Justicia",
        descKey = "Petricita resplandeciente, honor inquebrantable, azul real justiciero y oro protector.",
        background = Color(0xFF030A17),
        surface = Color(0xFF081830),
        surfaceVariant = Color(0xFF0F264A),
        cardBorder = Color(0xFF1E4B8A),
        primary = Color(0xFF2563EB),
        primaryLight = Color(0xFF60A5FA),
        primaryDark = Color(0xFF1D4ED8),
        primaryGlow = Color(0xFF3B82F6),
        secondary = Color(0xFFFDE047),
        secondaryLight = Color(0xFFFEF08A),
        secondaryDark = Color(0xFFCA8A04),
        secondaryGlow = Color(0xFFFDF08A),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF93C5FD),
        textMuted = Color(0xFF64748B),
        isDark = true
    ),

    // 5. El Vacío (The Void) - Dimensión corruptora: Magenta corruptor y Bioluminiscencia Neón Cian
    EL_VACIO(
        id = "EL_VACIO",
        titleKey = "El Vacío",
        regionTag = "La Nada Hambrienta",
        descKey = "Terror cósmico insaciable, púrpura bioluminiscente, magenta alienígena y oscuridad corruptora.",
        background = Color(0xFF08010E),
        surface = Color(0xFF160424),
        surfaceVariant = Color(0xFF26083C),
        cardBorder = Color(0xFF541285),
        primary = Color(0xFFD946EF),
        primaryLight = Color(0xFFF0ABFC),
        primaryDark = Color(0xFFA21CAF),
        primaryGlow = Color(0xFFE879F9),
        secondary = Color(0xFF8B5CF6),
        secondaryLight = Color(0xFFC4B5FD),
        secondaryDark = Color(0xFF5B21B6),
        secondaryGlow = Color(0xFFA78BFA),
        textPrimary = Color(0xFFFCE7F3),
        textSecondary = Color(0xFFC084FC),
        textMuted = Color(0xFF7E22CE),
        isDark = true
    ),

    // 6. Freljord - Tierras del Hielo Puro: Cian glacial congelado y Fuego Ancestral de la Forja de Ornn
    FRELJORD(
        id = "FRELJORD",
        titleKey = "Freljord",
        regionTag = "Tierras del Norte",
        descKey = "Hielo puro ancestral, escarcha ártica implacable, azul glacial y templanza de los Hijos del Hielo.",
        background = Color(0xFF030914),
        surface = Color(0xFF071829),
        surfaceVariant = Color(0xFF0E2741),
        cardBorder = Color(0xFF1A4670),
        primary = Color(0xFF00E5FF),
        primaryLight = Color(0xFF84FFFF),
        primaryDark = Color(0xFF00B8D4),
        primaryGlow = Color(0xFF18FFFF),
        secondary = Color(0xFF93C5FD),
        secondaryLight = Color(0xFFBFDBFE),
        secondaryDark = Color(0xFF2563EB),
        secondaryGlow = Color(0xFF60A5FA),
        textPrimary = Color(0xFFF0F9FF),
        textSecondary = Color(0xFF93C5FD),
        textMuted = Color(0xFF4B6B94),
        isDark = true
    ),

    // 7. Islas de la Sombra - Niebla Negra y Almas Malditas: Jade espectral y Púrpura de Niebla Negra
    ISLAS_DE_LA_SOMBRA(
        id = "ISLAS_DE_LA_SOMBRA",
        titleKey = "Islas de la Sombra",
        regionTag = "Niebla Negra",
        descKey = "Tierras consumidas por la Ruina, jade espectral fulgurante, almas atormentadas y frío de ultratumba.",
        background = Color(0xFF010A0A),
        surface = Color(0xFF041818),
        surfaceVariant = Color(0xFF082626),
        cardBorder = Color(0xFF114848),
        primary = Color(0xFF00FFB3),
        primaryLight = Color(0xFF6EE7B7),
        primaryDark = Color(0xFF059669),
        primaryGlow = Color(0xFF34D399),
        secondary = Color(0xFF14B8A6),
        secondaryLight = Color(0xFF5EEAD4),
        secondaryDark = Color(0xFF0F766E),
        secondaryGlow = Color(0xFF2DD4BF),
        textPrimary = Color(0xFFE6FFFA),
        textSecondary = Color(0xFF5EEAD4),
        textMuted = Color(0xFF115E59),
        isDark = true
    ),

    // 8. Ixtal - Selva Elemental Ancestral: Esmeralda axiomática y Turquesa Elemental de Ríos
    IXTAL(
        id = "IXTAL",
        titleKey = "Ixtal",
        regionTag = "Magia Elemental",
        descKey = "Dominio de los axiomas elementales, selva densa esmeralda, orquídeas salvajes y bronce arcano.",
        background = Color(0xFF020C06),
        surface = Color(0xFF071F11),
        surfaceVariant = Color(0xFF0F331D),
        cardBorder = Color(0xFF195732),
        primary = Color(0xFF10B981),
        primaryLight = Color(0xFF34D399),
        primaryDark = Color(0xFF047857),
        primaryGlow = Color(0xFF6EE7B7),
        secondary = Color(0xFFF59E0B),
        secondaryLight = Color(0xFFFCD34D),
        secondaryDark = Color(0xFFB45309),
        secondaryGlow = Color(0xFFFBBF24),
        textPrimary = Color(0xFFECFDF5),
        textSecondary = Color(0xFFA7F3D0),
        textMuted = Color(0xFF4B8B67),
        isDark = true
    ),

    // 9. Jonia - Las Tierras Primigenias: Rosa Flor de Loto y Verde Jade Espiritual de Armonía
    JONIA(
        id = "JONIA",
        titleKey = "Jonia",
        regionTag = "Tierras Primigenias",
        descKey = "Equilibrio supremo de la naturaleza, flores de loto sagradas, magia espiritual y cerezos en flor.",
        background = Color(0xFF0C050D),
        surface = Color(0xFF1C0E20),
        surfaceVariant = Color(0xFF2C1632),
        cardBorder = Color(0xFF52285E),
        primary = Color(0xFFF472B6),
        primaryLight = Color(0xFFFBCFE8),
        primaryDark = Color(0xFFBE185D),
        primaryGlow = Color(0xFFF9A8D4),
        secondary = Color(0xFF38BDF8),
        secondaryLight = Color(0xFFBAE6FD),
        secondaryDark = Color(0xFF0284C7),
        secondaryGlow = Color(0xFF7DD3FC),
        textPrimary = Color(0xFFFDF2F8),
        textSecondary = Color(0xFFF472B6),
        textMuted = Color(0xFF834E79),
        isDark = true
    ),

    // 10. Noxus - Imperio Conquistador: Rojo Carmesí de Sangre y Acero Forjado Oscuro
    NOXUS(
        id = "NOXUS",
        titleKey = "Noxus",
        regionTag = "Imperio de la Fuerza",
        descKey = "Fuerza militar implacable, rojo sangre de conquista, acero forjado y triunfo de los fuertes.",
        background = Color(0xFF0B0204),
        surface = Color(0xFF19060A),
        surfaceVariant = Color(0xFF26090F),
        cardBorder = Color(0xFF4A121A),
        primary = Color(0xFFEF4444),
        primaryLight = Color(0xFFF87171),
        primaryDark = Color(0xFF991B1B),
        primaryGlow = Color(0xFFFCA5A5),
        secondary = Color(0xFFF97316),
        secondaryLight = Color(0xFFFDBA74),
        secondaryDark = Color(0xFFC2410C),
        secondaryGlow = Color(0xFFFB923C),
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
        background = Color(0xFF0E0701),
        surface = Color(0xFF211303),
        surfaceVariant = Color(0xFF351F06),
        cardBorder = Color(0xFF613B0E),
        primary = Color(0xFFFFB300),
        primaryLight = Color(0xFFFFCA28),
        primaryDark = Color(0xFFFF8F00),
        primaryGlow = Color(0xFFFFD54F),
        secondary = Color(0xFF2DD4BF),
        secondaryLight = Color(0xFF99F6E4),
        secondaryDark = Color(0xFF0F766E),
        secondaryGlow = Color(0xFF5EEAD4),
        textPrimary = Color(0xFFFFF7ED),
        textSecondary = Color(0xFFFDBA74),
        textMuted = Color(0xFF8A6538),
        isDark = true
    ),

    // 12. Targon - La Cumbre Celestial: Violeta Cósmico y Oro Solar Divino de los Solari
    TARGON(
        id = "TARGON",
        titleKey = "Targon",
        regionTag = "Cumbre Celestial",
        descKey = "Morada de los Aspectos Celestiales, violeta estelar profundo, plata lunar y fuego solar divino.",
        background = Color(0xFF060312),
        surface = Color(0xFF120C29),
        surfaceVariant = Color(0xFF1F1545),
        cardBorder = Color(0xFF3B2875),
        primary = Color(0xFF7C3AED),
        primaryLight = Color(0xFFA78BFA),
        primaryDark = Color(0xFF5B21B6),
        primaryGlow = Color(0xFF8B5CF6),
        secondary = Color(0xFFFBBF24),
        secondaryLight = Color(0xFFFDE68A),
        secondaryDark = Color(0xFFD97706),
        secondaryGlow = Color(0xFFFCD34D),
        textPrimary = Color(0xFFF3E5F5),
        textSecondary = Color(0xFFA594C9),
        textMuted = Color(0xFF6E5D91),
        isDark = true
    ),

    // 13. Zaun - Ciudad Subterránea Quimtech: Verde Quimtech Ácido Neón y Púrpura Shimmer Químico
    ZAUN(
        id = "ZAUN",
        titleKey = "Zaun",
        regionTag = "Subciudad Quimtech",
        descKey = "Ingenio desenfrenado en las profundidades, verde ácido químico, humo Zaun y resplandor Shimmer.",
        background = Color(0xFF020B06),
        surface = Color(0xFF061A0F),
        surfaceVariant = Color(0xFF0B2B19),
        cardBorder = Color(0xFF144D2B),
        primary = Color(0xFF39FF14),
        primaryLight = Color(0xFF86EFAC),
        primaryDark = Color(0xFF166534),
        primaryGlow = Color(0xFF4ADE80),
        secondary = Color(0xFFEAB308),
        secondaryLight = Color(0xFFFEF08A),
        secondaryDark = Color(0xFFA16207),
        secondaryGlow = Color(0xFFFACC15),
        textPrimary = Color(0xFFE8FDF0),
        textSecondary = Color(0xFF7EBF96),
        textMuted = Color(0xFF4D8A65),
        isDark = true
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

object AppThemeManager {
    private const val PREFS_KEY_THEME = "selected_app_theme_id"
    private const val PREFS_KEY_NAV_BAR = "selected_app_navbar_id"
    private const val PREFS_KEY_OLED_MODE = "selected_app_oled_mode"

    var currentTheme by mutableStateOf(AppTheme.PILTOVER)
        private set

    var currentNavBarOption by mutableStateOf(NavBarColorOption.THEME_AUTO)
        private set

    var isOledMode by mutableStateOf(false)
        private set

    var isParticlesEnabled by mutableStateOf(false)
        private set

    fun init(context: Context) {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, "app_prefs_enc")
        val savedThemeId = prefs.getString(PREFS_KEY_THEME, AppTheme.PILTOVER.id) ?: AppTheme.PILTOVER.id
        val savedNavId = prefs.getString(PREFS_KEY_NAV_BAR, NavBarColorOption.THEME_AUTO.id) ?: NavBarColorOption.THEME_AUTO.id
        val savedOled = prefs.getBoolean(PREFS_KEY_OLED_MODE, false)
        val savedParticles = prefs.getBoolean("particles_enabled", false)

        currentTheme = AppTheme.fromId(savedThemeId)
        currentNavBarOption = NavBarColorOption.entries.find { it.id == savedNavId } ?: NavBarColorOption.THEME_AUTO
        isOledMode = savedOled
        isParticlesEnabled = savedParticles
    }

    fun setOledMode(enabled: Boolean, context: Context? = null) {
        isOledMode = enabled
        context?.let {
            val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(it, "app_prefs_enc")
            prefs.edit().putBoolean(PREFS_KEY_OLED_MODE, enabled).apply()
        }
    }
    
    fun setParticlesEnabled(enabled: Boolean, context: Context? = null) {
        isParticlesEnabled = enabled
        context?.let {
            val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(it, "app_prefs_enc")
            prefs.edit().putBoolean("particles_enabled", enabled).apply()
        }
    }

    fun setTheme(theme: AppTheme, context: Context? = null) {
        currentTheme = theme
        context?.let {
            val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(it, "app_prefs_enc")
            prefs.edit().putString(PREFS_KEY_THEME, theme.id).apply()
        }
    }

    fun setNavBarOption(option: NavBarColorOption, context: Context? = null) {
        currentNavBarOption = option
        context?.let {
            val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(it, "app_prefs_enc")
            prefs.edit().putString(PREFS_KEY_NAV_BAR, option.id).apply()
        }
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
