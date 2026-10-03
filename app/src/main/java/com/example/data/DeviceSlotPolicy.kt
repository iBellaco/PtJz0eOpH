package com.example.data

object DeviceSlotPolicy {
    fun register(existing: List<String>, current: String, aliases: Set<String>, administrator: Boolean): List<String> {
        val devices = existing.map { if (it in aliases) current else it }.filter { it.isNotBlank() }.distinct().toMutableList()
        if (current !in devices) {
            check(administrator || devices.size < 2) { "Límite de dispositivos alcanzado (Máx 2 dispositivos por cuenta)." }
            if (administrator && devices.size >= 10) devices.removeAt(0)
            devices += current
        }
        return devices
    }
}
