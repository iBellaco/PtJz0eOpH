package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.data.sync.ChineseMetaSyncService
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class MetaRegionRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    @Before fun prepare() { WildRiftRepository.initChampions(context, forceReload = true) }
    @After fun finish() { scope.cancel() }

    @Test fun `selector order and initial choice match the restored lists`() {
        context.getSharedPreferences("app_prefs", 0).edit().clear().commit()
        ChineseMetaSyncService.loadRegion(context)
        assertEquals(listOf("GLOBAL", "CN"), MetaRegion.available)
        assertEquals("GLOBAL", ChineseMetaSyncService.currentRegion.value)
        assertEquals("GLOBAL", MetaRegion.normalize("auto"))
        assertTrue(MetaRegion.label("CN").startsWith("🇨🇳"))
        assertTrue(MetaRegion.label("GLOBAL").startsWith("🌐"))
    }
    @Test fun `explicit selection persists after reload`() {
        for (region in MetaRegion.available) {
            ChineseMetaSyncService.setRegion(context, region, scope)
            ChineseMetaSyncService.loadRegion(context)
            assertEquals(region, ChineseMetaSyncService.currentRegion.value)
            assertEquals(region, WildRiftRepository.activeRegionName)
        }
    }
    @Test fun `region switching restores distinct published snapshots`() {
        val baseline = WildRiftRepository.champions.toList()
        val id = baseline.first().id
        WildRiftRepository.applyRegionalTierList("GLOBAL", mapOf(RegionalTierParser.canonical(id) to "S+"))
        val china = baseline.map { it.copy(tier = "C", cnTier = "T4", hasRegionalStats = true, winrate = 55.5) }
        WildRiftRepository.applyChineseStats(china)
        WildRiftRepository.selectMetaRegion("GLOBAL")
        assertEquals("S+", WildRiftRepository.champions.first().tier)
        assertFalse(WildRiftRepository.champions.first().hasRegionalStats)
        WildRiftRepository.selectMetaRegion("CN")
        assertEquals(china, WildRiftRepository.champions.toList())
        WildRiftRepository.selectMetaRegion("GLOBAL")
        assertEquals("S+", WildRiftRepository.champions.first().tier)
        assertEquals(0.0, WildRiftRepository.champions.first().winrate, 0.0)
    }
    @Test fun `late Chinese response cannot replace a selected Global list`() {
        val baseline = WildRiftRepository.champions.toList()
        WildRiftRepository.applyRegionalTierList("GLOBAL", mapOf(RegionalTierParser.canonical(baseline.first().id) to "A"))
        WildRiftRepository.selectMetaRegion("GLOBAL")
        val visible = WildRiftRepository.champions.toList()
        WildRiftRepository.applyChineseStats(baseline.map { it.copy(winrate = 59.0, tier = "S+") })
        assertEquals(visible, WildRiftRepository.champions.toList())
    }
    @Test fun `page parser extracts categories rather than inventing match statistics`() {
        val html = """<div class="tier splus"><a href="/guide/syndra" class="ico-holder" data-role="Mid">Syndra</a>
            <div class="tier s"><a href="/guide/garen" class="ico-holder">Garen</a>
            <div class="tier b"><a href="/guide/syndra" class="ico-holder">Syndra</a>"""
        assertEquals(mapOf("syndra" to "S+", "garen" to "S"), RegionalTierParser.parse(html))
        assertTrue(RegionalTierParser.parse("<html>Service unavailable</html>").isEmpty())
    }
    @Test fun `unsupported server regions cannot produce a fabricated regional top list`() {
        assertEquals("GLOBAL", MetaRegion.normalize("NA"))
        assertEquals("GLOBAL", MetaRegion.normalize("EU"))
        assertEquals("—", com.example.util.regionalPercent(WildRiftRepository.champions.first().copy(hasRegionalStats = false), 50.0))
    }
}
