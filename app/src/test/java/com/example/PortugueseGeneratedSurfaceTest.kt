package com.example

import android.app.Application
import com.example.data.SituationalItemAdvisor
import com.example.data.WildRiftRepository
import com.example.data.local.CustomChampionBuildsManager
import com.example.util.*
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PortugueseGeneratedSurfaceTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @Before fun prepare() {
        DynamicTranslations.loadSync(context)
        AppLanguage.select(context, "pt")
        WildRiftRepository.initChampions(context, forceReload = true)
        CustomChampionBuildsManager.init(context)
    }

    @Test fun `all equipment advice and generated champion role profiles are Portuguese`() {
        val values = linkedMapOf<String, String>()
        fun inspect(origin: String, texts: Iterable<String>) {
            texts.forEach { source -> values.putIfAbsent(trStr("pt", source), origin) }
        }
        for (item in WildRiftRepository.items) {
            inspect("item-details/${item.id}", listOf(item.getLocalizedName("pt"), item.getLocalizedStats("pt"), item.getLocalizedPassive("pt"), item.getLocalizedCoachTip("pt")))
            val advice = SituationalItemAdvisor.getAdvice(item.name, "pt")
            inspect("item/${item.name}", listOf(advice.name, advice.categoryName, advice.purpose,
                advice.keyEffect, advice.recommendationTip) + advice.bestAgainst)
        }
        var profiles = 0
        WildRiftRepository.runes.forEach { inspect("rune/${it.id}", listOf(it.getLocalizedName("pt"), it.getLocalizedDescription("pt"))) }
        WildRiftRepository.summonerSpells.forEach { inspect("spell/${it.id}", listOf(it.getLocalizedName("pt"), it.getLocalizedDescription("pt"))) }
        for (champion in WildRiftRepository.champions) {
            champion.skills.forEach { inspect("skill/${champion.id}/${it.slot}", listOf(it.getLocalizedName("pt"), it.getLocalizedDescription("pt"))) }
            for (role in (listOf(champion.primaryRole) + champion.secondaryRoles).distinct()) {
                val profile = ChampionRoleAdapter.getProfile(champion, role)
                profiles++
                inspect("profile/${champion.id}/${role.name}", listOf(profile.tacticalAdvice, profile.recommendedRunes, profile.runeTreeDetails))
                profile.itemSwaps.forEach { inspect("swap/${champion.id}", listOf(it.reasonTitle, it.reasonDesc, it.againstWho)) }
                profile.buildOptions.forEach { option ->
                    inspect("build/${champion.id}", listOf(option.title, option.subtitle, option.source, option.badge, option.tacticalReason))
                    inspect("build-items/${champion.id}", (option.coreItemsWithDesc + option.situationalItemsWithDesc).map { it.description })
                }
            }
        }
        assertTrue(profiles > 100)
        File("build/reports/portuguese-rendered").apply { mkdirs() }.resolve("generated-texts.json")
            .writeText(JSONArray(values.map { (text, origin) -> JSONObject().put("origin", origin).put("text", text) }).toString(2))
        val failures = values.filterKeys { SpanishUiResidue.pattern.containsMatchIn(it.replace("Lee Sin", "LeeSin")) }
        failures.forEach { (value, origin) -> println("PORTUGUESE_GENERATED_RESIDUE: $origin: $value") }
        assertTrue("Generated Spanish remains:\n${failures.entries.joinToString("\n") { "${it.value}: ${it.key}" }}", failures.isEmpty())
        println("PORTUGUESE_GENERATED_AUDIT: $profiles profiles; ${values.size} distinct texts")
    }

    @Test fun `premium duration translates assembled units before displaying them`() {
        val duration = SubscriptionManager.formatDuration(System.currentTimeMillis() + (367L * 24 * 60 * 60 + 3661) * 1000)
        assertTrue(duration, duration.contains("ano"))
        assertTrue(duration, duration.contains("dias"))
        assertFalse(duration, SpanishUiResidue.pattern.containsMatchIn(duration))
        AppLanguage.select(context, "es")
        val spanish = SubscriptionManager.formatDuration(System.currentTimeMillis() + 2L * 24 * 60 * 60 * 1000 + 10000)
        assertTrue(spanish, spanish.contains("días"))
    }
}
