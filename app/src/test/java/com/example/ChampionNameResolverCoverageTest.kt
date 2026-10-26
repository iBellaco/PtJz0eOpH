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
    @Test fun scannerSnapshotSurvivesCatalogReplacementDuringIteration() {
        WildRiftRepository.initChampions(RuntimeEnvironment.getApplication(), forceReload = true)
        val snapshot = WildRiftRepository.championSnapshot()
        val ids = snapshot.map { it.id }
        assertEquals(142, ids.size)
        val iterator = snapshot.iterator()
        val first = iterator.next()
        // Deterministically interrupt iteration with the same replacement that
        // background regional metadata performs. A live StateList iterator fails.
        androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
            WildRiftRepository.champions.clear()
            WildRiftRepository.champions.addAll(snapshot.reversed())
        }
        try {
            assertEquals(ids, listOf(first.id) + iterator.asSequence().map { it.id }.toList())
            assertEquals("vex", ChampionNameResolver.findChampionInNameBand("VEX",snapshot)?.id)
            assertEquals(ids.reversed(),WildRiftRepository.championSnapshot().map { it.id })
            snapshot.forEach { champion ->
                assertEquals(champion.id,ChampionNameResolver.findChampionInNameBand(champion.name,snapshot)?.id)
            }
        } finally {
            androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                WildRiftRepository.champions.clear()
                WildRiftRepository.champions.addAll(snapshot)
            }
        }
    }
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
