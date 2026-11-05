package com.example

import android.app.Application
import android.graphics.Bitmap
import com.example.model.Champion
import com.example.service.OverlayState
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.TenthPickDiagnosticManager as Crops
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
class DraftSessionLifecycleTest {
    @Test fun clearClosesChampionAndRejectsResultsFromPreviousScanAfterRestart() {
        val ctx = RuntimeEnvironment.getApplication()
        val hud = OverlayState()
        hud.autoScanEnabled = true
        val previous = hud.scanGeneration
        hud.selectedChampionDetail = Champion(id="sona")
        hud.showChampionPickerForSlot = true to 0
        hud.clearDraftAndCrops(ctx)
        assertNull(hud.selectedChampionDetail)
        assertNull(hud.showChampionPickerForSlot)
        assertFalse(hud.acceptsScanResult(previous))
        hud.startAutomaticScan()
        try {
            assertFalse(hud.acceptsScanResult(previous))
            assertTrue(hud.acceptsScanResult(hud.scanGeneration))
        } finally { hud.autoScanEnabled = false }
    }
    @Test fun mainHubClearPreservesFiveEmptyRoleSlotsPerTeam() {
        val session = com.example.ui.screens.DraftSessionManager
        session.clearAll()
        assertEquals(5, session.allySlots.size)
        assertEquals(5, session.enemySlots.size)
        assertEquals(5, session.allySlots.map { it.assignedRole }.distinct().size)
        assertTrue((session.allySlots + session.enemySlots).all {
            it.champion == com.example.data.WildRiftRepository.EMPTY_CHAMPION
        })
    }
    @Test fun diagnosticEnabledCannotWriteAfterAutomaticScanStops() {
        val ctx = RuntimeEnvironment.getApplication()
        val hud = OverlayState()
        val bitmap = Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888)
        try {
            Crops.init(ctx); Crops.setDiagnosticMode(ctx,true); Crops.clearAllCaches(ctx)
            hud.autoScanEnabled=false
            assertNull(Crops.recordTenthPickCrop(bitmap,true,4,"paused",context=ctx))
            hud.autoScanEnabled=true
            assertNotNull(Crops.recordTenthPickCrop(bitmap,true,4,"active",context=ctx))
            hud.autoScanEnabled=false
            assertNull(Crops.recordTenthPickCrop(bitmap,true,4,"stopped",context=ctx))
            assertEquals(1,Crops.getDiagnosticCacheDir(ctx).listFiles { _,name->name.endsWith(".png") }!!.size)
        } finally { hud.autoScanEnabled=false; Crops.clearAllCaches(ctx); bitmap.recycle() }
    }
    @Test fun hubClearRemovesFilesStatisticsAndOldLabelsAndStopsCapture() {
        val ctx=RuntimeEnvironment.getApplication();val hud=OverlayState()
        val bitmap=Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888)
        try {
            Crops.init(ctx);Crops.setDiagnosticMode(ctx,true);hud.autoScanEnabled=true
            Crops.recordTenthPickCrop(bitmap,false,4,"before_clear",context=ctx)
            hud.allies[0]=Champion(id="sona",name="Sona")
            DraftVisionScanner.debugVisualMatches.value=mapOf("ally_0" to "Sona")
            DraftVisionScanner.overlayRect=android.graphics.Rect(1,2,20,22)
            DraftVisionScanner.debugTextRects=listOf(android.graphics.Rect(2,3,10,12))
            hud.clearDraftAndCrops(ctx)
            assertFalse(hud.autoScanEnabled)
            assertTrue(hud.allies.all { it==null } && hud.enemies.all { it==null })
            assertTrue(DraftVisionScanner.debugVisualMatches.value.isEmpty())
            assertNull(DraftVisionScanner.overlayRect)
            assertTrue(DraftVisionScanner.debugTextRects.isEmpty())
            assertTrue(Crops.savedFramesFlow.value.isEmpty())
            assertEquals(0,Crops.cacheStatsFlow.value.totalFiles)
            assertTrue(Crops.getDiagnosticCacheDir(ctx).listFiles()!!.isEmpty())
            assertNull(Crops.recordTenthPickCrop(bitmap,false,4,"after_clear",context=ctx))
        } finally { hud.autoScanEnabled=false;Crops.clearAllCaches(ctx);bitmap.recycle() }
    }
    @Test fun automaticSessionDropsUnrepresentedOldReadingsAndKeepsManuallyLockedPicks() {
        val hud=OverlayState()
        val manual=Champion(id="aurora",name="Aurora")
        hud.allies[1]=manual
        hud.manualLockedAllySlots[1]=true
        DraftVisionScanner.allySlotConfirmedChampions[0]=Champion(id="sona",name="Sona")
        DraftVisionScanner.debugVisualMatches.value=mapOf("ally_0" to "Sona")
        DraftVisionScanner.showCalibrationBoxes.value=true
        hud.startAutomaticScan()
        assertNull(hud.allies[0]);assertEquals(manual,hud.allies[1])
        assertTrue(DraftVisionScanner.allySlotConfirmedChampions.all { it==null })
        assertTrue(DraftVisionScanner.debugVisualMatches.value.isEmpty())
        assertTrue(DraftVisionScanner.showCalibrationBoxes.value)
        DraftVisionScanner.showCalibrationBoxes.value=false
        hud.autoScanEnabled=false
    }
}
