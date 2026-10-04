package com.example.data

/** Optional scoreboard values, stored as kills/deaths/assists rather than arbitrary notes. */
object DraftScoreFormat {
    private val complete = Regex("[0-9]{1,3}/[0-9]{1,3}/[0-9]{1,3}")
    fun isValid(value: String) = value.isBlank() || complete.matches(value.trim())
    fun acceptsInput(value: String) = value.length <= 11 && value.all { it in '0'..'9' || it == '/' } && value.count { it == '/' } <= 2
    fun normalize(value: String): String {
        require(isValid(value)) { "Score inválido: usa eliminaciones/muertes/asistencias." }
        return if (value.isBlank()) "" else value.trim().split('/').joinToString("/") { it.toInt().toString() }
    }
}
