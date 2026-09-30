package com.example.util

fun regionalPercent(champion: com.example.model.Champion, value: Double): String =
    if (!champion.hasRegionalStats) {
        "—"
    } else if (value > 0.0) {
        String.format(java.util.Locale.US, "%.2f%%", value)
    } else if (champion.winrate > 0.0) {
        String.format(java.util.Locale.US, "%.2f%%", champion.winrate)
    } else {
        "—"
    }


