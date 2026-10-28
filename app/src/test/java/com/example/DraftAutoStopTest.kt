package com.example

import android.app.Application
import com.example.model.Champion
import com.example.service.OverlayState
import com.example.service.applyConfirmedLastPick
import com.example.service.screen.DraftScanResult
import com.example.service.screen.TenthPickDiagnosticManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
class DraftAutoStopTest {
    @Test fun ninthAndUnconfirmedPortraitKeepScanningAndDepartureRequestsOneSave() {
        val hud=OverlayState()
        (0..4).forEach { hud.allies[it]=Champion(id="ally_$it",name="Ally $it") }
        (0..3).forEach { hud.enemies[it]=Champion(id="enemy_$it",name="Enemy $it") }
        val ninth=(hud.allies+hud.enemies).filterNotNull().map {it.id}.toSet()
        val final=Champion(id="enemy_4",name="Final")
        hud.autoScanEnabled=true
        try {
            assertFalse(hud.stopAutomaticScanIfComplete())
            val report=DraftScanResult(allies=hud.allies.filterNotNull(),enemies=hud.enemies.filterNotNull(),
                lastPickChampion=final,tenthPickIsAlly=false,tenthPickSlotIndex=4,
                isLastPickConfirmed=false,isSuccessful=true,statusMessage="")
            hud.applyConfirmedLastPick(report)
            assertTrue(hud.autoScanEnabled)
            assertNull(hud.enemies[4])
            hud.applyConfirmedLastPick(report.copy(isLastPickConfirmed=true))
            assertEquals(final,hud.enemies[4])
            assertFalse(hud.showSaveDraftDialog)
            assertTrue(hud.autoScanEnabled)
            assertFalse(hud.autoSavePendingRequested)
            assertFalse(hud.stopAutomaticScanIfComplete(confirmed=true))
            assertTrue(hud.stopAutomaticScanIfComplete(selectionEnded=true))
            assertTrue(hud.autoSavePendingRequested)
            hud.applyConfirmedLastPick(report.copy(isLastPickConfirmed=true,
                lastPickChampion=Champion(id="late_report",name="Late report")))
            assertEquals("Completed draft must remain intact until automatic persistence",final,hud.enemies[4])
            hud.autoSavePendingRequested = false
            hud.applyConfirmedLastPick(report.copy(isLastPickConfirmed=true))
            assertFalse("A repeated report must not request another automatic save",hud.autoSavePendingRequested)
            assertTrue((hud.allies+hud.enemies).filterNotNull().map {it.id}.toSet().containsAll(ninth))
            assertFalse(hud.autoScanEnabled)
            assertFalse(TenthPickDiagnosticManager.automaticScanActiveFlow.value)
        } finally { hud.autoScanEnabled=false }
    }

    @Test fun sionChangesToGwenOnEitherSideBeforeDeparture() {
        for (ally in listOf(true, false)) {
            val hud = OverlayState()
            val own = if (ally) hud.allies else hud.enemies
            val other = if (ally) hud.enemies else hud.allies
            repeat(4) { own[it] = Champion(id="own$it") }
            repeat(5) { other[it] = Champion(id="other$it") }
            val originalNine = (own.take(4) + other).map { it!!.id }
            hud.autoScanEnabled = true
            try {
                fun observe(id: String, confirmed: Boolean = true) = hud.applyConfirmedLastPick(
                    DraftScanResult(emptyList(), emptyList(), isSuccessful=true, statusMessage="",
                        lastPickChampion=Champion(id=id), tenthPickIsAlly=ally, tenthPickSlotIndex=4,
                        isLastPickConfirmed=confirmed))
                observe("sion")
                assertTrue(own.any { it?.id == "sion" })
                observe("gwen", false)
                assertTrue(own.any { it?.id == "sion" })
                observe("gwen")
                assertTrue(own.any { it?.id == "gwen" })
                assertFalse(own.any { it?.id == "sion" })
                assertEquals(originalNine.toSet(), (own + other).filterNotNull().map { it.id }.toSet() - "gwen")
                if (ally) assertEquals(originalNine, (own.take(4) + other).map { it!!.id })
                assertTrue(hud.autoScanEnabled)
                assertFalse(hud.autoSavePendingRequested)
                assertTrue(hud.stopAutomaticScanIfComplete(selectionEnded=true))
                assertTrue(hud.autoSavePendingRequested)
                hud.autoSavePendingRequested = false
                observe("sion")
                assertTrue(own.any { it?.id == "gwen" })
                assertFalse(hud.stopAutomaticScanIfComplete(selectionEnded=true))
                assertFalse(hud.autoSavePendingRequested)
            } finally { hud.autoScanEnabled = false }
        }
    }

    @Test fun duplicateOrIncompleteSelectionsCannotStopAutomaticScan() {
        val hud=OverlayState()
        hud.autoScanEnabled=true
        try {
            (0..4).forEach { hud.allies[it]=Champion(id="same",name="Same"); hud.enemies[it]=Champion(id="enemy_$it",name="Enemy") }
            assertFalse(hud.stopAutomaticScanIfComplete(confirmed=true, selectionEnded=true))
            assertTrue(hud.autoScanEnabled)
        } finally { hud.autoScanEnabled=false }
    }
}
