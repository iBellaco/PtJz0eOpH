package com.example.data

/** Versioned plain-text envelope; contains only the requested display name and device geometry, never credentials or screenshots. */
object CalibrationReport {
    const val MARKER = "[COACH_CALIBRATION_V2]"
    const val END = "[/COACH_CALIBRATION_V2]"
    private const val LEGACY_MARKER = "[COACH_CALIBRATION_V1]"
    private const val LEGACY_END = "[/COACH_CALIBRATION_V1]"
    private const val LIMIT = 16000

    fun title(isPremium: Boolean, language: String): String {
        val pt = language.equals("pt", ignoreCase = true)
        val base = if (pt) "Coordenadas do scanner" else "Coordenadas del escáner"
        val tier = when {
            isPremium && pt -> "Usuário Premium"
            isPremium -> "Usuario Premium"
            pt -> "Usuário gratuito"
            else -> "Usuario gratuito"
        }
        return "$base · $tier"
    }

    fun build(
        device: String,
        app: String,
        display: String,
        capture: String,
        coordinates: String,
        userName: String = "Usuario",
        language: String = "es",
        androidVersion: String = "",
        detectionEvidence: String = ""
    ): String {
        fun field(value: String) = value.replace('\n', ' ').replace('\r', ' ').take(200)
        require(coordinates.isNotBlank() && coordinates.length < 12000)
        require(!coordinates.contains(MARKER) && !coordinates.contains(END))
        val cleanDevice = field(device).substringBefore(';').trim()
        val cleanDisplay = field(display).substringBefore(';').trim()
        val cleanAndroid = field(androidVersion).ifBlank {
            Regex("API\\s+([0-9]+)", RegexOption.IGNORE_CASE).find(field(device))?.value
                ?: "No disponible"
        }
        val cleanUser = field(userName).takeUnless { it.contains("@") || it.equals("Invocador", ignoreCase = true) }
            ?.ifBlank { null } ?: if (language.equals("pt", ignoreCase = true)) "Usuário" else "Usuario"
        val pt = language.equals("pt", ignoreCase = true)
        val userLabel = if (pt) "Usuário" else "Usuario"
        val screenLabel = if (pt) "Tela" else "Pantalla"
        val coordinatesLabel = if (pt) "Coordenadas" else "Coordenadas"
        val cleanCoordinates = coordinates.lineSequence()
            .filterNot { it.trimStart().startsWith("usuario=", ignoreCase = true) }
            .joinToString("\n").trim()
        val cleanEvidence = detectionEvidence
            .replace(MARKER, "")
            .replace(END, "")
            .trim()
            .take(3500)
        return buildString {
            appendLine(MARKER)
            appendLine("COORDENADAS")
            appendLine("$userLabel: $cleanUser")
            appendLine("Dispositivo: $cleanDevice")
            appendLine("$screenLabel: $cleanDisplay")
            appendLine("Android: $cleanAndroid")
            appendLine("$coordinatesLabel:")
            appendLine(cleanCoordinates)
            if (cleanEvidence.isNotBlank()) {
                appendLine()
                appendLine(cleanEvidence)
            }
            append(END)
        }
    }

    /**
     * Returns the short human-readable payload shown in the support list and
     * conversation. The version markers remain available for copying/parsing,
     * but are not useful to the person reviewing the coordinates.
     */
    fun displayText(text: String): String? {
        val envelope = extract(text) ?: return null
        val firstBreak = envelope.indexOf('\n')
        if (firstBreak < 0) return null
        val body = envelope.substring(firstBreak + 1)
        val end = body.lastIndexOf('\n')
        return body.substring(0, if (end >= 0) end else body.length)
            .lineSequence()
            .filterNot { line ->
                val normalized = line.trim().uppercase(java.util.Locale.ROOT)
                normalized.startsWith("---") ||
                    normalized.startsWith("ETIQUETA:") ||
                    normalized.startsWith("CAPTURA ANALIZADA:") ||
                    normalized.startsWith("CAPTURA ANALISADA:") ||
                    normalized.startsWith("COACH:")
            }
            .joinToString("\n")
            .trim()
            .takeIf { it.isNotBlank() }
    }

    fun extract(text: String): String? {
        fun between(marker: String, endMarker: String): String? {
            val start = text.indexOf(marker).takeIf { it >= 0 } ?: return null
            val end = text.indexOf(endMarker, start + marker.length).takeIf { it >= 0 } ?: return null
            if (end + endMarker.length - start > LIMIT) return null
            return text.substring(start, end + endMarker.length)
        }
        return between(MARKER, END) ?: between(LEGACY_MARKER, LEGACY_END)
    }
}
