package com.example

import android.app.Application
import android.graphics.BitmapFactory
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.service.screen.ChampionNameResolver
import com.example.service.screen.DraftValidationLayer
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChampionNameResolverRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private var previous: List<Champion> = emptyList()

    @Before fun loadRealCatalog() {
        previous = WildRiftRepository.champions.toList()
        WildRiftRepository.initChampions(context, forceReload = true)
    }
    @After fun restore() {
        WildRiftRepository.champions.clear()
        WildRiftRepository.champions.addAll(previous)
    }

    @Test fun shortViOcrVariantsResolveToVi() {
        for (name in listOf("Vi", "vl", "v1"))
            assertEquals("vi", ChampionNameResolver.findChampionInText(name, WildRiftRepository.champions)?.id)
    }

    @Test fun all142DisplayNamesResolveToTheRealCatalogEntryAndPortrait() {
        val catalog = WildRiftRepository.champions.toList()
        assertEquals(142, catalog.size)
        assertEquals(142, catalog.map { it.id }.distinct().size)
        for (champion in catalog) {
            assertFalse("Internal identifier shown as name: ${champion.id}", champion.name.contains('_'))
            for (text in listOf(champion.name, champion.name.uppercase(java.util.Locale.ROOT))) {
                assertEquals("Incorrect champion identity for $text", champion,
                    ChampionNameResolver.findChampionInText(text, catalog))
            }
            assertEquals("file:///android_asset/champions/${champion.id}.png", champion.avatarUrl)
            val bitmap = context.assets.open("champions/${champion.id}.png").use { BitmapFactory.decodeStream(it) }
            assertNotNull("Missing decoded portrait: ${champion.id}", bitmap)
            try { assertTrue(bitmap!!.width >= 16 && bitmap.height >= 16) }
            finally { bitmap?.recycle() }
        }
    }

    @Test fun nunuAliasesAndLegacySavedIdKeepNamePortraitAndLane() {
        val nunu = WildRiftRepository.getChampionById("nunu_willump")!!
        assertEquals("Nunu y Willump", nunu.name)
        for (alias in listOf("Nunu", "Nunu y Willump", "Nunu & Willump", "Nunu e Willump", "Nunu_and_willump")) {
            assertEquals(alias, nunu, ChampionNameResolver.findChampionInText(alias, WildRiftRepository.champions))
        }
        assertEquals(nunu, WildRiftRepository.getChampionById("nunu_and_willump"))
        assertEquals(nunu, WildRiftRepository.getBaseChampion("NUNU_AND_WILLUMP"))
        assertTrue(DraftValidationLayer.isValidChampionToken("nunu", nunu.id))
        assertFalse(DraftValidationLayer.isValidChampionToken("nunuito", nunu.id))
    }
}
