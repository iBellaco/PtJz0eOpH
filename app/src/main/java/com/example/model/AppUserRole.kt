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
    CREATOR_VIP(
        id = "creador_vip",
        displayName = "Creador VIP",
        emoji = "",
        primaryColor = Color(0xFFA855F7),
        secondaryColor = Color(0xFF7C3AED),
        description = "Creador destacado con acceso preferencial y distintivo"
    ),
    STREAMER(
        id = "streamer",
        displayName = "Streamer",
        emoji = "",
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFFDB2777),
        description = "Creador de directos, transmisiones y difusión de Coach"
    ),
    CREATOR(
        id = "creador",
        displayName = "Creador",
        emoji = "",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFD97706),
        description = "Colaborador de contenido, diseño y tácticas"
    ),
    PATROCINADOR(
        id = "patrocinador",
        displayName = "Patrocinador",
        emoji = "",
        primaryColor = Color(0xFFF97316),
        secondaryColor = Color(0xFFC2410C),
        description = "Patrocinador oficial con acceso a panel de anuncios CPM y presupuestos"
    ),
    PREMIUM(
        id = "premium",
        displayName = "Premium",
        emoji = "",
        primaryColor = Color(0xFF00F0FF),
        secondaryColor = Color(0xFF0284C7),
        description = "Pase Hextech activo con todas las herramientas de coaching"
    ),
    FREE(
        id = "free",
        displayName = "Gratis",
        emoji = "",
        primaryColor = Color(0xFF94A3B8),
        secondaryColor = Color(0xFF64748B),
        description = "Plan estándar con funciones básicas"
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
                "vip", "creador_vip", "creator_vip" -> CREATOR_VIP
                "streamer", "live" -> STREAMER
                "creador", "creator" -> CREATOR
                "patrocinador", "sponsor" -> PATROCINADOR
                "premium", "pro" -> PREMIUM
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
            MODERATOR,
            CREATOR_VIP,
            STREAMER,
            CREATOR,
            BANNED
        )
    }
}
