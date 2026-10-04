package com.example.data

object DeviceSlotPolicy {
    fun register(existing: List<String>, current: String, aliases: Set<String>, administrator: Boolean): List<String> {
        val devices = existing.map { if (it in aliases) current else it }.filter { it.isNotBlank() }.distinct().toMutableList()
        if (current !in devices) {
            check(devices.size < 2) { "Límite de dispositivos alcanzado (Máx 2 dispositivos por cuenta)." }
            devices += current
        }
        return devices
    }
}
