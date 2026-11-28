package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

enum class SeasonalEffect { CHRISTMAS, NEW_YEAR }

/** A month/day range evaluated in each device's local time zone. */
data class SeasonalDate(val month: Int, val day: Int) : Comparable<SeasonalDate> {
    init {
        require(month in 1..12)
        require(day in 1..daysInMonth(month))
    }

    override fun compareTo(other: SeasonalDate): Int = (month * 100 + day).compareTo(other.month * 100 + other.day)

    fun asText(): String = "%02d-%02d".format(java.util.Locale.ROOT, month, day)

    companion object {
        fun parse(value: String): SeasonalDate? {
            val match = Regex("^(\\d{2})-(\\d{2})$").matchEntire(value.trim()) ?: return null
            val month = match.groupValues[1].toIntOrNull() ?: return null
            val day = match.groupValues[2].toIntOrNull() ?: return null
            return runCatching { SeasonalDate(month, day) }.getOrNull()
        }

        fun daysInMonth(month: Int): Int = when (month) {
            2 -> 29
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }
}

data class SeasonalEventSettings(
    val enabled: Boolean = false,
    val scheduled: Boolean = true,
    val start: SeasonalDate,
    val end: SeasonalDate
) {
    fun isActiveOn(localDate: SeasonalDate): Boolean {
        if (!enabled) return false
        if (!scheduled) return true
        return if (start <= end) localDate in start..end else localDate >= start || localDate <= end
    }
}

data class SeasonalEffectsConfig(
    val christmas: SeasonalEventSettings = SeasonalEventSettings(
        start = SeasonalDate(12, 20), end = SeasonalDate(12, 26)
    ),
    val newYear: SeasonalEventSettings = SeasonalEventSettings(
        start = SeasonalDate(12, 30), end = SeasonalDate(1, 2)
    )
) {
    fun activeEffectsAt(localDate: SeasonalDate): Set<SeasonalEffect> = buildSet {
        if (christmas.isActiveOn(localDate)) add(SeasonalEffect.CHRISTMAS)
        if (newYear.isActiveOn(localDate)) add(SeasonalEffect.NEW_YEAR)
    }

    fun asFirestoreMap(): Map<String, Any> = mapOf(
        "christmasEnabled" to christmas.enabled,
        "christmasScheduled" to christmas.scheduled,
        "christmasStartMonth" to christmas.start.month,
        "christmasStartDay" to christmas.start.day,
        "christmasEndMonth" to christmas.end.month,
        "christmasEndDay" to christmas.end.day,
        "newYearEnabled" to newYear.enabled,
        "newYearScheduled" to newYear.scheduled,
        "newYearStartMonth" to newYear.start.month,
        "newYearStartDay" to newYear.start.day,
        "newYearEndMonth" to newYear.end.month,
        "newYearEndDay" to newYear.end.day
    )

    companion object {
        private fun Map<String, Any?>.bool(name: String, default: Boolean) = this[name] as? Boolean ?: default
        private fun Map<String, Any?>.number(name: String, default: Int) = (this[name] as? Number)?.toInt() ?: default
        private fun Map<String, Any?>.date(prefix: String, fallback: SeasonalDate): SeasonalDate = runCatching {
            SeasonalDate(number("${prefix}Month", fallback.month), number("${prefix}Day", fallback.day))
        }.getOrDefault(fallback)

        fun fromFirestore(data: Map<String, Any?>?): SeasonalEffectsConfig {
            if (data == null) return SeasonalEffectsConfig()
            val defaults = SeasonalEffectsConfig()
            return SeasonalEffectsConfig(
                christmas = SeasonalEventSettings(
                    enabled = data.bool("christmasEnabled", defaults.christmas.enabled),
                    scheduled = data.bool("christmasScheduled", defaults.christmas.scheduled),
                    start = data.date("christmasStart", defaults.christmas.start),
                    end = data.date("christmasEnd", defaults.christmas.end)
                ),
                newYear = SeasonalEventSettings(
                    enabled = data.bool("newYearEnabled", defaults.newYear.enabled),
                    scheduled = data.bool("newYearScheduled", defaults.newYear.scheduled),
                    start = data.date("newYearStart", defaults.newYear.start),
                    end = data.date("newYearEnd", defaults.newYear.end)
                )
            )
        }
    }
}

object SeasonalEffectsManager {
    private const val COLLECTION = "system_config"
    private const val DOCUMENT = "seasonal_effects"

    private val _config = MutableStateFlow(SeasonalEffectsConfig())
    val config: StateFlow<SeasonalEffectsConfig> = _config.asStateFlow()

    @Volatile private var listener: ListenerRegistration? = null

    /** Starts one shared listener so a published schedule reaches every device. */
    fun initialize() {
        if (listener != null) return
        synchronized(this) {
            if (listener != null) return
            listener = runCatching {
                FirebaseFirestore.getInstance().collection(COLLECTION).document(DOCUMENT)
                    .addSnapshotListener { snapshot, error ->
                        if (error == null) _config.value = SeasonalEffectsConfig.fromFirestore(snapshot?.data)
                    }
            }.getOrNull()
        }
    }

    suspend fun save(updated: SeasonalEffectsConfig) {
        FirebaseFirestore.getInstance().collection(COLLECTION).document(DOCUMENT)
            .set(updated.asFirestoreMap() + mapOf("updatedAt" to Timestamp.now()), SetOptions.merge())
            .await()
        _config.value = updated
    }

    fun localDate(calendar: Calendar = Calendar.getInstance()): SeasonalDate = SeasonalDate(
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
