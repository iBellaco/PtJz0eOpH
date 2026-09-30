package com.example.util

fun regionalPercent(champion: com.example.model.Champion, value: Double): String =
    if (champion.hasRegionalStats) String.format(java.util.Locale.US, "%.2f%%", value) else "—"
