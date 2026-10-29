package com.example.data

import android.content.Context
import org.json.JSONObject

/** Unconfirmed submissions survive process death, with their original id and quoted cost. */
data class SponsorSubmission(val notice: AppNotice, val expectedCost: Long)

class SponsorSubmissionStore(context: Context, private val uid: String) {
    private val prefs = context.applicationContext.getSharedPreferences("sponsor_outbox", Context.MODE_PRIVATE)
    fun read(): List<SponsorSubmission> = prefs.all.entries.filter { it.key.startsWith("$uid~") }
        .mapNotNull { (_, value) -> runCatching {
            val saved = JSONObject(value as String)
            val n = saved.getJSONObject("notice")
            SponsorSubmission(AppNotice(id = n.getString("id"), title = n.getString("title"), content = n.getString("content"),
                videoUrl = n.getString("videoUrl"), expandedImageUrl = n.getString("expandedImageUrl"),
                externalUrl = n.getString("externalUrl"), titleColor = n.getString("titleColor"),
                durationValue = n.getInt("durationValue"), durationUnit = n.getString("durationUnit"),
                budget = n.getDouble("budget"), budgetUnit = n.getString("durationUnit"),
                sponsorEmail = n.getString("sponsorEmail"), tag = "PUBLICIDAD", isApproved = false, isEnabled = false),
                saved.getLong("expectedCost"))
        }.getOrNull() }

    fun save(submission: SponsorSubmission): Boolean {
        val n = submission.notice
        val notice = JSONObject().put("id", n.id).put("title", n.title).put("content", n.content)
            .put("videoUrl", n.videoUrl).put("expandedImageUrl", n.expandedImageUrl).put("externalUrl", n.externalUrl)
            .put("titleColor", n.titleColor).put("durationValue", n.durationValue).put("durationUnit", n.durationUnit)
            .put("budget", n.budget).put("sponsorEmail", n.sponsorEmail)
        val saved = JSONObject().put("notice", notice).put("expectedCost", submission.expectedCost)
        return prefs.edit().putString("$uid~${n.id}", saved.toString()).commit()
    }

    fun complete(id: String): Boolean = prefs.edit().remove("$uid~$id").commit()
}
