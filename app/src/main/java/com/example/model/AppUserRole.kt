package com.example.model

import androidx.compose.ui.graphics.Color

enum class AppUserRole(
    val id: String,
    val displayName: String,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val description: String
) {
    ADMIN(
        id = "admin",
        displayName = "Administrador",
        emoji = "",
        primaryColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFB8860B),
        description = "Acceso total institucional y control de la plataforma"
    ),
    MODERATOR(
        id = "moderador",
        displayName = "Moderador",
        emoji = "",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF059669),
        description = "Moderación de OCR, reportes y soporte comunitario"
    ),
    CREATOR_LVL2(
        id = "creador_lvl2",
        displayName = "Creador Lvl 2",
        emoji = "",
        primaryColor = Color(0xFFFFA7D5),
        secondaryColor = Color(0xFFB35D92),
        description = "Creador Lvl 2: Creador avanzado (Hasta 150 subs, 3 campeones)"
    ),
    CREATOR_LVL3(
        id = "creador_lvl3",
        displayName = "Creador Lvl 3",
        emoji = "",
        primaryColor = Color(0xFFFF9160),
        secondaryColor = Color(0xFFC9653A),
        description = "Creador Lvl 3: Creador premium (Hasta 250 subs, 5 campeones)"
    ),
    CREATOR_LVL4(
        id = "creador_lvl4",
        displayName = "Creador Lvl 4",
        emoji = "",
        primaryColor = Color(0xFFD4EB72),
        secondaryColor = Color(0xFF8FAD35),
        description = "Creador Lvl 4: Creador master (Hasta 350 subs, 7 campeones)"
    ),
    CREATOR_LVL5(
        id = "creador_lvl5",
        displayName = "Creador Lvl 5",
        emoji = "",
        primaryColor = Color(0xFFF5D6C6),
        secondaryColor = Color(0xFFB78B77),
        description = "Creador Lvl 5: Creador de élite (Hasta 500 subs, 10 campeones)"
    ),
    STREAMER(
        id = "streamer",
        displayName = "Streamer",
        emoji = "",
        primaryColor = Color(0xFFFF74C8),
        secondaryColor = Color(0xFFB44791),
        description = "Creador de directos, transmisiones y difusión de Coach"
    ),
    CREATOR(
        id = "creador",
        displayName = "Creador Lvl 1",
        emoji = "",
        primaryColor = Color(0xFFFFE1B0),
        secondaryColor = Color(0xFFB98B54),
        description = "Creador Lvl 1: Creador oficial (Hasta 50 subs, 1 campeón)"
    ),
    PATROCINADOR(
        id = "patrocinador",
        displayName = "Patrocinador",
        emoji = "",
        primaryColor = Color(0xFFE7E2D9),
        secondaryColor = Color(0xFFA09A91),
        description = "Patrocinador oficial con acceso a panel de anuncios CPM y presupuestos"
    ),
    PREMIUM(
        id = "premium",
        displayName = "Premium",
        emoji = "",
        primaryColor = Color(0xFFB4BAFF),
        secondaryColor = Color(0xFF747CC6),
        description = "Pase Hextech activo con todas las herramientas de coaching"
    ),
    FREE(
        id = "free",
        displayName = "Gratis",
        emoji = "",
        primaryColor = Color(0xFFB9ADA3),
        secondaryColor = Color(0xFF82746A),
        description = "Plan estándar con funciones básicas"
    ),
    ESMERALDA(
        id = "esmeralda",
        displayName = "Esmeralda",
        emoji = "",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF047857),
        description = "Rol secundario sin privilegios de sistema"
    ),
    DIAMANTE(
        id = "diamante",
        displayName = "Diamante",
        emoji = "",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF0284C7),
        description = "Rol secundario sin privilegios de sistema"
    ),
    MAESTRO(
        id = "maestro",
        displayName = "Maestro",
        emoji = "",
        primaryColor = Color(0xFFA855F7),
        secondaryColor = Color(0xFF7C3AED),
        description = "Rol secundario sin privilegios de sistema"
    ),
    GRAN_MAESTRO(
        id = "gran_maestro",
        displayName = "Gran Maestro",
        emoji = "",
        primaryColor = Color(0xFFF43F5E),
        secondaryColor = Color(0xFFBE123C),
        description = "Rol secundario sin privilegios de sistema"
    ),
    ASPIRANTE(
        id = "aspirante",
        displayName = "Aspirante",
        emoji = "",
        primaryColor = Color(0xFFC88A61),
        secondaryColor = Color(0xFF389FF7),
        description = "Rol secundario sin privilegios de sistema"
    ),
    SOBERANO(
        id = "soberano",
        displayName = "Soberano",
        emoji = "",
        primaryColor = Color(0xFFD8B66F),
        secondaryColor = Color(0xFF78C6FF),
        description = "Rol secundario sin privilegios de sistema"
    ),
    BANNED(
        id = "banned",
        displayName = "Baneado",
        emoji = "",
        primaryColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFDC2626),
        description = "Cuenta suspendida y bloqueada por infracciones"
    );

    companion object {
        fun fromId(rawId: String?): AppUserRole {
            val normalized = rawId?.trim()?.lowercase() ?: return FREE
            return values().firstOrNull { it.id == normalized } ?: when (normalized) {
                "admin", "administrator" -> ADMIN
                "mod", "moderador", "moderator" -> MODERATOR
                "vip", "creador_vip", "creator_vip", "creador_lvl2", "creator_lvl2" -> CREATOR_LVL2
                "creador_lvl3" -> CREATOR_LVL3
                "creador_lvl4" -> CREATOR_LVL4
                "creador_lvl5" -> CREATOR_LVL5
                "streamer", "live" -> STREAMER
                "creador", "creator" -> CREATOR
                "patrocinador", "sponsor" -> PATROCINADOR
                "premium", "pro" -> PREMIUM
                "esmeralda" -> ESMERALDA
                "diamante" -> DIAMANTE
                "maestro" -> MAESTRO
                "gran_maestro", "gran maestro" -> GRAN_MAESTRO
                "aspirante" -> ASPIRANTE
                "soberano" -> SOBERANO
                "banned", "suspendido", "bloqueado" -> BANNED
                else -> FREE
            }
        }

        // Roles asignables desde el panel de gestión.
        // REGLA CRÍTICA: NO SE PERMITE ASIGNAR ADMIN. ADMIN ESTÁ ESTRICTAMENTE EXCLUIDO.
        val assignableRoles: List<AppUserRole> = listOf(
            FREE,
            PATROCINADOR,
            PREMIUM,
            ESMERALDA,
            DIAMANTE,
            MAESTRO,
            GRAN_MAESTRO,
            ASPIRANTE,
            SOBERANO,
            MODERATOR,
            CREATOR_LVL2,
            CREATOR_LVL3,
            CREATOR_LVL4,
            CREATOR_LVL5,
            STREAMER,
            CREATOR,
            BANNED
        )
    }
}
