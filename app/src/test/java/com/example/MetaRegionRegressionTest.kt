package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.data.sync.ChineseMetaSyncService
import com.example.data.sync.MetaRegion
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
    private val scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
    @Before fun prepare() {
        WildRiftRepository.initChampions(context, forceReload = true)
        WildRiftRepository.selectMetaRegion("GLOBAL")
        context.getSharedPreferences("app_prefs", 0).edit().remove("selected_meta_region").commit()
        ChineseMetaSyncService.loadRegion(context)
        assertEquals("GLOBAL", ChineseMetaSyncService.currentRegion.value)
    }
    @After fun finish() {
        scope.cancel()
        WildRiftRepository.selectMetaRegion("GLOBAL")
    }
    @Test fun `all three lists are selectable and selection is persisted`() {
        assertEquals(listOf("CN", "GLOBAL", "NA"), MetaRegion.available)
        for (region in listOf("GLOBAL", "NA")) {
            ChineseMetaSyncService.setRegion(context, region, scope)
            assertEquals(region, ChineseMetaSyncService.currentRegion.value)
            assertEquals(region, WildRiftRepository.activeRegionName)
            assertEquals(region, context.getSharedPreferences("app_prefs", 0).getString("selected_meta_region", ""))
            ChineseMetaSyncService.loadRegion(context)
            assertEquals(region, ChineseMetaSyncService.currentRegion.value)
        }
    }
    @Test fun `Chinese refresh cannot overwrite the visible local list`() {
        val baseline = WildRiftRepository.champions.toList()
        WildRiftRepository.selectMetaRegion("GLOBAL")
        val refreshed = baseline.map { it.copy(winrate = 55.5, pickRate = 12.0, banRate = 7.0) }
        WildRiftRepository.applyChineseStats(refreshed)
        assertEquals(baseline, WildRiftRepository.champions.toList())
        WildRiftRepository.selectMetaRegion("CN")
        assertEquals(refreshed, WildRiftRepository.champions.toList())
        WildRiftRepository.selectMetaRegion("NA")
        assertEquals(baseline, WildRiftRepository.champions.toList())
    }
    @Test fun `local reference numbers are stable across switches`() {
        val baseline = WildRiftRepository.champions.toList()
        repeat(3) {
            WildRiftRepository.selectMetaRegion("NA")
            assertEquals(baseline, WildRiftRepository.champions.toList())
            WildRiftRepository.selectMetaRegion("GLOBAL")
            assertEquals(baseline, WildRiftRepository.champions.toList())
        }
    }
}
