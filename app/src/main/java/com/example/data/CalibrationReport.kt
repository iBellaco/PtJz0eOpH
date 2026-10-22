package com.example.data

/** Versioned plain-text envelope; deliberately contains no account identifiers or screenshots. */
object CalibrationReport {
    const val MARKER = "[COACH_CALIBRATION_V1]"
    const val END = "[/COACH_CALIBRATION_V1]"
    private const val LIMIT = 16000

    fun build(device: String, app: String, display: String, capture: String, coordinates: String): String {
        fun field(value: String) = value.replace('\n', ' ').replace('\r', ' ').take(200)
        require(coordinates.isNotBlank() && coordinates.length < 12000)
        require(!coordinates.contains(MARKER) && !coordinates.contains(END))
        return "$MARKER\ndevice=${field(device)}\napp=${field(app)}\ndisplay=${field(display)}\ncapture=${field(capture)}\n$coordinates\n$END"
    }

    fun extract(text: String): String? {
        val start = text.indexOf(MARKER).takeIf { it >= 0 } ?: return null
        val end = text.indexOf(END, start + MARKER.length).takeIf { it >= 0 } ?: return null
        if (end + END.length - start > LIMIT) return null
        return text.substring(start, end + END.length)
    }
}
