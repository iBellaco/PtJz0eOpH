package com.example.data

/** Versioned plain-text envelope; contains only the requested display name and device geometry, never credentials or screenshots. */
object CalibrationReport {
    const val MARKER = "[COACH_CALIBRATION_V2]"
    const val END = "[/COACH_CALIBRATION_V2]"
    private const val LEGACY_MARKER = "[COACH_CALIBRATION_V1]"
    private const val LEGACY_END = "[/COACH_CALIBRATION_V1]"
    private const val LIMIT = 16000

    fun build(
        device: String,
        app: String,
        display: String,
        capture: String,
        coordinates: String,
        userName: String = "Usuario",
        language: String = "es"
    ): String {
        fun field(value: String) = value.replace('\n', ' ').replace('\r', ' ').take(200)
        require(coordinates.isNotBlank() && coordinates.length < 12000)
        require(!coordinates.contains(MARKER) && !coordinates.contains(END))
        val pt = language == "pt"
        val cleanCoordinates = coordinates.lineSequence()
            .filterNot { it.trimStart().startsWith("usuario=", ignoreCase = true) }
            .joinToString("\n").trim()
        return buildString {
            appendLine(MARKER)
            appendLine(if (pt) "ETIQUETA: COORDENADAS DO SCANNER — SOMENTE ADMINISTRAÇÃO" else "ETIQUETA: COORDENADAS DEL ESCÁNER — SOLO ADMINISTRACIÓN")
            appendLine("${if (pt) "Usuário" else "Usuario"}: ${field(userName)}")
            appendLine("${if (pt) "Dispositivo" else "Dispositivo"}: ${field(device)}")
            appendLine("${if (pt) "Tela" else "Pantalla"}: ${field(display)}")
            appendLine("${if (pt) "Captura analisada" else "Captura analizada"}: ${field(capture)}")
            appendLine("Coach: ${field(app)}")
            appendLine(if (pt) "--- COORDENADAS SALVAS ---" else "--- COORDENADAS GUARDADAS ---")
            appendLine(cleanCoordinates)
            append(END)
        }
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
