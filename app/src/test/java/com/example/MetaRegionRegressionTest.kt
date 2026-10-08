package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.data.sync.GlobalMetaSyncService
import com.example.data.sync.MetaRegion
import com.example.data.sync.RegionalTierParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.mockito.Mockito

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class MetaRegionRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    @Before fun prepare() { WildRiftRepository.initChampions(context, forceReload = true) }
    @After fun finish() { scope.cancel() }

    @Test fun `selector order and initial choice match the restored lists`() {
        context.getSharedPreferences("app_prefs", 0).edit().clear().commit()
        GlobalMetaSyncService.loadRegion(context)
        assertEquals(listOf("GLOBAL"), MetaRegion.available)
        assertEquals("GLOBAL", GlobalMetaSyncService.currentRegion.value)
        assertEquals("GLOBAL", MetaRegion.normalize("auto"))
        assertEquals("GLOBAL", MetaRegion.normalize("CN"))
        assertTrue(MetaRegion.label("GLOBAL").startsWith("🌐"))
    }
    @Test fun `saved Chinese preference migrates persistently to Global`() {
        val prefs = context.getSharedPreferences("app_prefs", 0)
        prefs.edit().putString("selected_meta_region", "CN").commit()
        GlobalMetaSyncService.loadRegion(context)
        assertEquals("GLOBAL", prefs.getString("selected_meta_region", ""))
        assertEquals("GLOBAL", WildRiftRepository.activeRegionName)
    }

    @Test fun `explicit selection persists after reload`() {
        for (region in MetaRegion.available) {
            GlobalMetaSyncService.setRegion(context, region, scope)
            GlobalMetaSyncService.loadRegion(context)
            assertEquals(region, GlobalMetaSyncService.currentRegion.value)
            assertEquals(region, WildRiftRepository.activeRegionName)
        }
    }
    @Test fun `legacy Chinese selection keeps the Global snapshot`() {
        val baseline = WildRiftRepository.champions.toList()
        val id = baseline.first().id
        WildRiftRepository.applyRegionalTierList("GLOBAL", mapOf(RegionalTierParser.canonical(id) to "S+"))
        WildRiftRepository.applyRegionalTierList("CN", mapOf(RegionalTierParser.canonical(id) to "C"))
        WildRiftRepository.selectMetaRegion("GLOBAL")
        assertEquals("S+", WildRiftRepository.champions.first().tier)
        assertFalse(WildRiftRepository.champions.first().hasRegionalStats)
        WildRiftRepository.selectMetaRegion("CN")
        assertEquals("GLOBAL", WildRiftRepository.activeRegionName)
        assertEquals("S+", WildRiftRepository.champions.first().tier)
        WildRiftRepository.selectMetaRegion("GLOBAL")
        assertEquals("S+", WildRiftRepository.champions.first().tier)
        assertEquals(baseline.first().winrate, WildRiftRepository.champions.first().winrate, 0.0)
        assertEquals(baseline.first().winrateDelta, WildRiftRepository.champions.first().winrateDelta, 0.0)
    }
    @Test fun `late Chinese response cannot replace a selected Global list`() {
        val baseline = WildRiftRepository.champions.toList()
        WildRiftRepository.applyRegionalTierList("GLOBAL", mapOf(RegionalTierParser.canonical(baseline.first().id) to "A"))
        WildRiftRepository.selectMetaRegion("GLOBAL")
        val visible = WildRiftRepository.champions.toList()
        WildRiftRepository.applyRegionalTierList("CN", mapOf(RegionalTierParser.canonical(baseline.first().id) to "S+"))
        assertEquals(visible, WildRiftRepository.champions.toList())
    }
    @Test fun `page parser extracts categories rather than inventing match statistics`() {
        val html = """<div class="tier splus"><a href="/guide/syndra" class="ico-holder" data-role="Mid">Syndra</a>
            </div><div class="tier s"><a href="/guide/garen" class="ico-holder">Garen</a>
            </div><div class="tier b"><a href="/guide/syndra" class="ico-holder">Syndra</a></div>"""
        assertEquals(mapOf("syndra" to "S+", "garen" to "S"), RegionalTierParser.parse(html))
        assertTrue(RegionalTierParser.parse("<html>Service unavailable</html>").isEmpty())
    }
    @Test fun `global categories preserve every catalog percentage instead of synthesizing statistics`() {
        val baseline = WildRiftRepository.baseChampionsList
        val tiers = baseline.associate { RegionalTierParser.canonical(it.id) to "S+" }
        WildRiftRepository.applyRegionalTierList("GLOBAL", tiers)
        val updated = WildRiftRepository.regionalSnapshot("GLOBAL")
        assertEquals(baseline.size, updated.size)
        baseline.zip(updated).forEach { (before, after) ->
            assertEquals(before.id, after.id)
            assertEquals("S+", after.tier)
            assertFalse(after.hasRegionalStats)
            assertEquals(before.winrate, after.winrate, 0.0)
            assertEquals(before.pickRate, after.pickRate, 0.0)
            assertEquals(before.banRate, after.banRate, 0.0)
        }
    }
    @Test fun `missing catalog percentages stay missing after category synchronization`() {
        val fixtureContext = Mockito.mock(android.content.Context::class.java)
        val fixtureResources = Mockito.mock(android.content.res.Resources::class.java)
        Mockito.`when`(fixtureContext.resources).thenReturn(fixtureResources)
        Mockito.`when`(fixtureResources.openRawResource(R.raw.champions_part1)).thenReturn(
            """[{"id":"fixture","name":"Fixture","winrate":0.0,"pickRate":0.0,"banRate":0.0}]""".byteInputStream())
        Mockito.`when`(fixtureResources.openRawResource(R.raw.champions_part2)).thenReturn("[]".byteInputStream())
        try {
            WildRiftRepository.initChampions(fixtureContext, forceReload = true)
            assertEquals("fixture", WildRiftRepository.champions.single().id)
            WildRiftRepository.applyRegionalTierList("GLOBAL", mapOf("fixture" to "S+"))
            val champion = WildRiftRepository.champions.single()
            assertEquals("S+", champion.tier)
            assertFalse(champion.hasRegionalStats)
            assertEquals(0.0, champion.winrate, 0.0)
            assertEquals(0.0, champion.pickRate, 0.0)
            assertEquals(0.0, champion.banRate, 0.0)
        } finally {
            WildRiftRepository.initChampions(context, forceReload = true)
        }
    }
    @Test fun `unsupported server regions cannot produce a fabricated regional top list`() {
        assertEquals("GLOBAL", MetaRegion.normalize("NA"))
        assertEquals("GLOBAL", MetaRegion.normalize("EU"))
        assertEquals("—", com.example.util.regionalPercent(WildRiftRepository.champions.first().copy(hasRegionalStats = false), 50.0))
    }
}
