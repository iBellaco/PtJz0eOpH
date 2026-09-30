package com.example.util

fun regionalPercent(champion: com.example.model.Champion, value: Double): String =
    if (value > 0.0) {
        String.format(java.util.Locale.US, "%.2f%%", value)
    } else if (champion.hasRegionalStats && value >= 0.0) {
        String.format(java.util.Locale.US, "%.2f%%", value)
    } else {
        "—"
    }


