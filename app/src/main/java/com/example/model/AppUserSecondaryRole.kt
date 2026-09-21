package com.example.model

import androidx.compose.ui.graphics.Color

enum class AppUserSecondaryRole(
    val id: String,
    val displayName: String,
    val tag: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val borderColor: Color,
    val tierLevel: Int,
    val description: String
) {
    NONE(
        id = "none",
        displayName = "Sin Rol Secundario",
        tag = "NINGUNO",
        primaryColor = Color(0xFF64748B),
        secondaryColor = Color(0xFF334155),
        borderColor = Color(0xFF475569),
        tierLevel = 0,
        description = "Sin rol secundario o rango asignado"
    ),
    HIERRO(
        id = "hierro",
        displayName = "Hierro",
        tag = "HIERRO",
        primaryColor = Color(0xFF94A3B8),
        secondaryColor = Color(0xFF64748B),
        borderColor = Color(0xFF475569),
        tierLevel = 1,
        description = "Invocador rango Hierro en Wild Rift"
    ),
    BRONCE(
        id = "bronce",
        displayName = "Bronce",
        tag = "BRONCE",
        primaryColor = Color(0xFFCD7F32),
        secondaryColor = Color(0xFF8C5320),
        borderColor = Color(0xFFA0522D),
        tierLevel = 2,
        description = "Invocador rango Bronce en Wild Rift"
    ),
    PLATA(
        id = "plata",
        displayName = "Plata",
        tag = "PLATA",
        primaryColor = Color(0xFFE2E8F0),
        secondaryColor = Color(0xFF94A3B8),
        borderColor = Color(0xFFCBD5E1),
        tierLevel = 3,
        description = "Invocador rango Plata en Wild Rift"
    ),
    ORO(
        id = "oro",
        displayName = "Oro",
        tag = "ORO",
        primaryColor = Color(0xFFFBBF24),
        secondaryColor = Color(0xFFD97706),
        borderColor = Color(0xFFF59E0B),
        tierLevel = 4,
        description = "Invocador rango Oro en Wild Rift"
    ),
    PLATINO(
        id = "platino",
        displayName = "Platino",
        tag = "PLATINO",
        primaryColor = Color(0xFF2DD4BF),
        secondaryColor = Color(0xFF0D9488),
        borderColor = Color(0xFF14B8A6),
        tierLevel = 5,
        description = "Invocador rango Platino en Wild Rift"
    ),
    ESMERALDA(
        id = "esmeralda",
        displayName = "Esmeralda",
        tag = "ESMERALDA",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF047857),
        borderColor = Color(0xFF34D399),
        tierLevel = 6,
        description = "Rol secundario: Invocador de rango Esmeralda de alto nivel"
    ),
    DIAMANTE(
        id = "diamante",
        displayName = "Diamante",
        tag = "DIAMANTE",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF0284C7),
        borderColor = Color(0xFF7DD3FC),
        tierLevel = 7,
        description = "Rol secundario: Invocador de rango Diamante de élite competitiva"
    ),
    MAESTRO(
        id = "maestro",
        displayName = "Maestro",
        tag = "MAESTRO",
        primaryColor = Color(0xFFC084FC),
        secondaryColor = Color(0xFF7E22CE),
        borderColor = Color(0xFFE879F9),
        tierLevel = 8,
        description = "Rol secundario: Invocador de rango Maestro superior"
    ),
    GRAN_MAESTRO(
        id = "gran_maestro",
        displayName = "Gran Maestro",
        tag = "GRAN MAESTRO",
        primaryColor = Color(0xFFF43F5E),
        secondaryColor = Color(0xFFBE123C),
        borderColor = Color(0xFFFB7185),
        tierLevel = 9,
        description = "Rol secundario: Invocador de rango Gran Maestro de nivel profesional"
    ),
    ASPIRANTE(
        id = "aspirante",
        displayName = "Aspirante",
        tag = "ASPIRANTE",
        primaryColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFB45309),
        borderColor = Color(0xFFFDE047),
        tierLevel = 10,
        description = "Rol secundario: Invocador Aspirante (Challenger) cumbre regional"
    ),
    SOBERANO(
        id = "soberano",
        displayName = "Soberano",
        tag = "SOBERANO",
        primaryColor = Color(0xFF00F0FF),
        secondaryColor = Color(0xFF4338CA),
        borderColor = Color(0xFFA5F3FC),
        tierLevel = 11,
        description = "Rol secundario: Invocador de rango Soberano máximo escalafón de Wild Rift"
    );

    companion object {
        fun fromId(rawId: String?): AppUserSecondaryRole {
            val normalized = rawId?.trim()?.lowercase()?.replace(" ", "_")?.replace("-", "_") ?: return NONE
            return values().firstOrNull { it.id == normalized } ?: when (normalized) {
                "soberano", "sovereign" -> SOBERANO
                "aspirante", "challenger", "retador" -> ASPIRANTE
                "gran_maestro", "granmaestro", "grandmaster", "gm" -> GRAN_MAESTRO
                "maestro", "master" -> MAESTRO
                "diamante", "diamond", "diamante+" -> DIAMANTE
                "esmeralda", "emerald" -> ESMERALDA
                "platino", "platinum" -> PLATINO
                "oro", "gold" -> ORO
                "plata", "silver" -> PLATA
                "bronce", "bronze" -> BRONCE
                "hierro", "iron" -> HIERRO
                else -> NONE
            }
        }

        // Roles secundarios destacados prioritarios solicitados
        val highEloSecondaryRoles: List<AppUserSecondaryRole> = listOf(
            ESMERALDA,
            DIAMANTE,
            MAESTRO,
            GRAN_MAESTRO,
            ASPIRANTE,
            SOBERANO
        )

        // Lista completa de asignación
        val assignableSecondaryRoles: List<AppUserSecondaryRole> = listOf(
            NONE,
            ESMERALDA,
            DIAMANTE,
            MAESTRO,
            GRAN_MAESTRO,
            ASPIRANTE,
            SOBERANO,
            PLATINO,
            ORO,
            PLATA,
            BRONCE,
            HIERRO
        )
    }
}
