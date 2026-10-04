package com.example.data

/** SSAID belongs to the device, Android user and signing key; reinstalling preserves it. */
object DeviceIdentityPolicy {
    fun resolve(androidId: String?, savedId: String?): String {
        val hardware = androidId?.trim()?.lowercase(java.util.Locale.ROOT)
        if (!hardware.isNullOrBlank() && hardware != "9774d56d682e549c") return "WRD_DEVICE_$hardware"
        // Never allocate an installation UUID that consumes another hardware slot.
        return savedId?.takeIf { it.isNotBlank() }
            ?: error("No se pudo identificar este dispositivo. Vuelve a intentarlo.")
    }
}

/** Token ownership, rather than client clocks, decides which session remains active. */
object DeviceSessionPolicy {
    fun owns(localToken: String?, localDevice: String, remoteToken: String?, remoteDevice: String?): Boolean =
        !localToken.isNullOrBlank() && localToken == remoteToken && localDevice == remoteDevice

    fun displaced(localToken: String?, localDevice: String, remoteToken: String?, remoteDevice: String?, active: Boolean): Boolean =
        active && !remoteToken.isNullOrBlank() && !remoteDevice.isNullOrBlank() &&
            !owns(localToken, localDevice, remoteToken, remoteDevice)
}
