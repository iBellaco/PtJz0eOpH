package com.example.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Limits explicit diagnostic submissions on this device to avoid accidental
 * support spam. The counter is local, resets at the device's local midnight,
 * and only a successful reservation consumes a slot.
 */
object DeviceDiagnosticQuota {
    const val MAX_PER_DAY = 5

    data class Usage(val used: Int, val remaining: Int)

    class Reservation internal constructor(internal val date: String)

    private const val PREFS_NAME = "device_diagnostic_quota"
    private const val DATE_KEY = "date"
    private const val COUNT_KEY = "count"
    private val lock = Any()

    fun usage(context: Context): Usage = synchronized(lock) {
        val prefs = preferences(context)
        val today = today()
        val count = normalizedCount(prefs, today)
        Usage(count, (MAX_PER_DAY - count).coerceAtLeast(0))
    }

    /** Reserves one send slot, or returns null after the daily limit is used. */
    fun tryReserve(context: Context): Reservation? = synchronized(lock) {
        val prefs = preferences(context)
        val date = today()
        val count = normalizedCount(prefs, date)
        if (count >= MAX_PER_DAY) return null
        prefs.edit()
            .putString(DATE_KEY, date)
            .putInt(COUNT_KEY, count + 1)
            .apply()
        Reservation(date)
    }

    /** Returns a reserved slot when the request failed before being submitted. */
    fun rollback(context: Context, reservation: Reservation) = synchronized(lock) {
        val prefs = preferences(context)
        if (prefs.getString(DATE_KEY, null) != reservation.date) return
        val count = prefs.getInt(COUNT_KEY, 0).coerceIn(0, MAX_PER_DAY)
        if (count > 0) prefs.edit().putInt(COUNT_KEY, count - 1).apply()
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun normalizedCount(
        prefs: android.content.SharedPreferences,
        date: String
    ): Int {
        if (prefs.getString(DATE_KEY, null) != date) {
            prefs.edit().putString(DATE_KEY, date).putInt(COUNT_KEY, 0).apply()
            return 0
        }
        return prefs.getInt(COUNT_KEY, 0).coerceIn(0, MAX_PER_DAY)
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
