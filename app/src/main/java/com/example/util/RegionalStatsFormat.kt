package com.example.util

fun regionalPercent(champion: com.example.model.Champion, value: Double): String =
    // Every champion ships with a validated local baseline. A temporarily
    // incomplete publisher response must not turn that usable value into a
    // blank field while the next automatic sync is pending.
    if (value.isFinite() && value in 0.0..100.0)
        String.format(java.util.Locale.US, "%.2f%%", value) else "—"

fun championStatPercent(champion: com.example.model.Champion, rawValue: Double, metricType: String = "wr"): String =
    regionalPercent(champion, rawValue)
