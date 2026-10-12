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
    @Test fun ninthSelectionAndProvisionalPortraitKeepScanningFinalConfirmedSelectionStops() {
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
            assertTrue("A stable preview can still change",hud.autoScanEnabled)
            val lastVisible=Champion(id="last_visible",name="Last visible")
            hud.applyConfirmedLastPick(report.copy(isLastPickConfirmed=true,isLastPickFinalized=true,lastPickChampion=lastVisible))
            assertEquals(lastVisible,hud.enemies[4])
            assertTrue((hud.allies+hud.enemies).filterNotNull().map {it.id}.toSet().containsAll(ninth))
            assertFalse(hud.autoScanEnabled)
            assertFalse(TenthPickDiagnosticManager.automaticScanActiveFlow.value)
        } finally { hud.autoScanEnabled=false }
    }

    @Test fun duplicateOrIncompleteSelectionsCannotStopAutomaticScan() {
        val hud=OverlayState()
        hud.autoScanEnabled=true
        try {
            (0..4).forEach { hud.allies[it]=Champion(id="same",name="Same"); hud.enemies[it]=Champion(id="enemy_$it",name="Enemy") }
            assertFalse(hud.stopAutomaticScanIfComplete(finalized=true))
            assertTrue(hud.autoScanEnabled)
        } finally { hud.autoScanEnabled=false }
    }
}
