package com.example.data

/** Recover actual legacy gifts from the original notice, never from the current balance. */
object AccountHistoryPolicy {
    fun notificationReceipt(message: Map<String, Any>): Map<String, Any>? {
        @Suppress("UNCHECKED_CAST")
        (message["historyRecord"] as? Map<String, Any>)?.let { return it }
        val id = (message["id"] as? String)?.takeIf(String::isNotBlank) ?: return null
        val title = message["title"] as? String ?: return null
        val addition = title == "¡Recompensa de Esencias!"
        if (!addition && title != "Ajuste de Saldo de Esencias") return null
        val content = (message["content"] as? String).orEmpty()
        val match = Regex("([+-]\\d+)\\s+Esencias (Azules|Naranjas) \\((EA|EN)\\)").find(content) ?: return null
        val timestamp = com.example.model.PremiumAccessPolicy.deadline(message["timestamp"])?.takeIf { it > 0 } ?: return null
        return mapOf("id" to id, "timestamp" to timestamp, "durationMillis" to 0L,
            "planName" to if (addition) "Regalo de Esencias" else "Ajuste de Esencias",
            "status" to if (addition) "Añadido por Administrador" else "Descontado por Administrador",
            "amount" to "${match.groupValues[1]} ${match.groupValues[3]}", "source" to "ADMIN_ESSENCE_ADJUSTMENT")
    }
}
