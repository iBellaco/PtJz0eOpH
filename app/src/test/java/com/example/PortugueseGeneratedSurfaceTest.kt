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
                val rival = WildRiftRepository.champions.first { it.id != champion.id }
                val duel = ChampionMatchupCoaching.forDuel(champion, rival, role, "pt")
                inspect("duel/${champion.id}/${role.name}", listOf(duel.early, duel.ultimate, duel.late, duel.rivalHeading,
                    duel.rival, duel.winCondition, duel.verdict))
                inspect("coaching/${champion.id}/${role.name}", listOf(CoachingGenerator.generateTacticalAnalysis(champion, role, "pt")))
                val recommendation = WildRiftRepository.evaluateChampion(champion, role,
                    WildRiftRepository.champions.filter { it.id != champion.id }.take(2),
                    WildRiftRepository.champions.filter { it.id != champion.id }.takeLast(2),
                    enemyLaneOpponent = WildRiftRepository.champions.first { it.id != champion.id }, lang = "pt")
                inspect("draft-advice/${champion.id}/${role.name}", listOf(recommendation.tacticalReason,
                    recommendation.advantageBadge, recommendation.synergyDetails, recommendation.counterDetails))
                val synergy = com.example.data.SynergyAdvisor.getSynergyProfile(champion, role, "pt")
                inspect("synergy/${champion.id}/${role.name}", listOf(synergy.archetype, synergy.archetypeBadge, synergy.archetypeDesc) + synergy.coreStrengths)
                synergy.bestTeammates.forEach { teammate ->
                    inspect("synergy-pair/${champion.id}/${teammate.championId}",
                        listOf(teammate.category, teammate.synergyTitle, teammate.tacticalReason, teammate.comboTips))
                }
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

    @Test fun `duel plans reflect every champion kit and both photographed matchups differ`() {
        val champions = WildRiftRepository.champions
        val enemy = champions.first { it.id == "smolder" }
        val varus = ChampionMatchupCoaching.forDuel(champions.first { it.id == "varus" }, enemy, com.example.model.LaneRole.ADC, "es")
        val jhin = ChampionMatchupCoaching.forDuel(champions.first { it.id == "jhin" }, enemy, com.example.model.LaneRole.ADC, "es")
        for ((a, b) in listOf(varus.early to jhin.early, varus.ultimate to jhin.ultimate, varus.late to jhin.late,
            varus.rival to jhin.rival, varus.winCondition to jhin.winCondition, varus.verdict to jhin.verdict)) assertNotEquals(a, b)
        assertTrue(varus.early.contains("tres marcas")); assertTrue(jhin.early.contains("cuarto disparo"))
        for (champion in champions) {
            val rival = champions.first { it.id != champion.id }
            val other = champions.last { it.id != champion.id && it.id != rival.id }
            val first = ChampionMatchupCoaching.forDuel(champion, rival, champion.primaryRole, "pt")
            val second = ChampionMatchupCoaching.forDuel(champion, other, champion.primaryRole, "pt")
            assertNotEquals(champion.id, first, second)
            for (text in listOf(first.early, first.ultimate, first.late, first.winCondition, first.verdict)) {
                assertTrue(champion.id, text.contains(champion.getLocalizedName("pt")))
                assertTrue(champion.id, text.contains(rival.getLocalizedName("pt")))
                assertFalse(text, text.contains("35-50"))
            }
            assertTrue(first.ultimate, first.ultimate.contains(champion.skills.first { it.slot == "4" }.getLocalizedName("pt")))
        }
    }

    @Test fun `kit interactions distinguish projectile blocking spell shields and invulnerability`() {
        fun champion(id: String) = WildRiftRepository.champions.first { it.id == id }
        fun plan(own: String, enemy: String) = ChampionMatchupCoaching.forDuel(champion(own), champion(enemy), champion(own).primaryRole, "pt")
        val shield = plan("varus", "morgana").verdict
        assertTrue(shield, shield.contains(champion("morgana").skills.first { it.slot == "3" }.getLocalizedName("pt")))
        assertTrue(shield, shield.contains("proteção"))
        val projectile = plan("yasuo", "morgana").verdict
        assertTrue(projectile, projectile.contains(champion("yasuo").skills.first { it.slot == "2" }.getLocalizedName("pt")))
        assertTrue(projectile, projectile.contains("projétil"))
        val invulnerability = plan("jhin", "kayle").verdict
        assertTrue(invulnerability, invulnerability.contains(champion("kayle").skills.first { it.slot == "4" }.getLocalizedName("pt")))
        assertTrue(invulnerability, invulnerability.contains("invulnerabilidade"))
    }

    @Test fun `personal coaching verdicts localize every grade and lane`() {
        val manager = com.example.data.analytics.PersonalTierListManager
        val method = manager::class.java.getDeclaredMethod("generateCoachVerdict", String::class.java,
            java.lang.Double.TYPE, java.lang.Integer.TYPE, com.example.model.LaneRole::class.java, String::class.java)
            .apply { isAccessible = true }
        val texts = com.example.model.LaneRole.entries.flatMap { role ->
            listOf(0.0, 25.0, 40.0, 55.0, 65.0, 80.0).map { winrate ->
                method.invoke(manager, "Ahri", winrate, if (winrate == 0.0) 0 else 20, role, "pt") as String
            }
        }
        texts.forEach { assertFalse(it, SpanishUiResidue.pattern.containsMatchIn(it)) }
        File("build/reports/portuguese-rendered").apply { mkdirs() }.resolve("personal-coaching-verdicts.json")
            .writeText(JSONArray(texts).toString(2))
    }

    @Test fun `automatic support acknowledgement and errors are Portuguese`() {
        val texts = listOf(com.example.data.SupportConversationPolicy.SYSTEM_GREETING,
            "Ticket cerrado", "Espera la primera respuesta del equipo", "No se pudo sincronizar el mensaje",
            "No se pudo sincronizar el estado")
        texts.forEach { text ->
            val translated = trStr("pt", text)
            assertNotEquals(text, translated)
            assertFalse(translated, SpanishUiResidue.pattern.containsMatchIn(translated))
        }
        assertEquals("Olá. O sistema recebeu sua mensagem. A equipe do Coach responderá aqui.",
            trStr("pt", com.example.data.SupportConversationPolicy.SYSTEM_GREETING))
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
