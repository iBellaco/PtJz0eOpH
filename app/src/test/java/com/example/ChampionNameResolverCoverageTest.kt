package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.service.screen.ChampionNameResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Verifies that every bundled champion title can enter the same exact-title resolver. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ChampionNameResolverCoverageTest {
    @Test fun everyBundledChampionHasAnExactTitleResolution() {
        WildRiftRepository.initChampions(RuntimeEnvironment.getApplication(), forceReload = true)
        val champions = WildRiftRepository.baseChampionsList
        assertEquals(142, champions.size)
        champions.forEach { champion ->
            val resolved = ChampionNameResolver.findChampionInNameBand(
                champion.name.uppercase(), champions)
            assertNotNull("No se resolvió el título completo de ${champion.name}", resolved)
            assertEquals(champion.id, resolved!!.id)
        }
    }
}
