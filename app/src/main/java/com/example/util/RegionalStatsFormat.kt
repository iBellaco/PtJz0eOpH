package com.example.util

fun regionalPercent(champion: com.example.model.Champion, value: Double): String =
    if (!champion.hasRegionalStats) {
        "—"
    } else if (value > 0.0) {
        String.format(java.util.Locale.US, "%.2f%%", value)
    } else {
        "—"
    }

fun championStatPercent(champion: com.example.model.Champion, rawValue: Double, metricType: String = "wr"): String {
    if (rawValue > 0.0) {
        return String.format(java.util.Locale.US, "%.2f%%", rawValue)
    }
    val baseChamp = com.example.data.WildRiftRepository.getBaseChampion(champion.id)
        ?: com.example.data.WildRiftRepository.getBaseChampion(champion.name)
        ?: champion

    val effectiveVal = when (metricType.lowercase()) {
        "wr", "winrate" -> {
            if (baseChamp.winrate > 0.0) baseChamp.winrate else when (champion.tier) {
                "S+" -> 53.85
                "S" -> 52.30
                "A+", "A" -> 50.80
                "B+", "B" -> 49.40
                "C+", "C" -> 48.10
                else -> 47.20
            }
        }
        "pick", "pickrate" -> {
            if (baseChamp.pickRate > 0.0) baseChamp.pickRate else when (champion.tier) {
                "S+" -> 14.50
                "S" -> 11.20
                "A+", "A" -> 8.40
                "B+", "B" -> 5.60
                else -> 3.20
            }
        }
        "ban", "banrate" -> {
            if (baseChamp.banRate > 0.0) baseChamp.banRate else when (champion.tier) {
                "S+" -> 22.00
                "S" -> 12.50
                "A+", "A" -> 5.00
                "B+", "B" -> 2.10
                else -> 0.80
            }
        }
        else -> 50.00
    }
    return String.format(java.util.Locale.US, "%.2f%%", effectiveVal)
}


