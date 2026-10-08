package com.example

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.ui.components.LiteRTEngineViewerDialog
import com.example.util.AppLanguage
import org.junit.Before
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class LiteRTViewerNavigationTest {
    @get:Rule val compose = createComposeRule()
    @Before fun prepare() { AppLanguage.select(RuntimeEnvironment.getApplication(), "es") }

    @After fun clearScanner() { com.example.service.screen.DraftVisionScanner.resetSlotMemory() }

    @Test fun `Spanish capture shortcuts preserve paused scan and detected draft`() = exerciseShortcuts("es", false)

    @Test fun `Portuguese capture shortcuts preserve active scan and detected draft`() = exerciseShortcuts("pt", true)

    @Test fun `resuming scan preserves detections role mapping and current report`() {
        val state = overlayState(false)
        val scanner = com.example.service.screen.DraftVisionScanner
        val report = com.example.service.screen.LiteRTVisionClassifier.reportFlow.value
        renderOverlay(state)
        compose.onNodeWithTag("overlay_auto_scan_toggle").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertTrue(state.autoScanEnabled)
            assertEquals("yuumi", scanner.allySlotConfirmedChampions[0]?.id)
            assertEquals("darius", scanner.enemySlotConfirmedChampions[1]?.id)
            assertEquals(com.example.model.LaneRole.SUPPORT, scanner.allyRolesBySlotFlow.value[0])
            assertEquals(report, com.example.service.screen.LiteRTVisionClassifier.reportFlow.value)
        }
    }

    private fun overlayState(active: Boolean): com.example.service.OverlayState {
        val context = RuntimeEnvironment.getApplication()
        com.example.util.DynamicTranslations.loadSync(context)
        com.example.data.WildRiftRepository.initChampions(context)
        val scanner = com.example.service.screen.DraftVisionScanner
        scanner.resetSlotMemory()
        val yuumi = com.example.model.Champion(id = "yuumi", name = "Yuumi")
        val darius = com.example.model.Champion(id = "darius", name = "Darius")
        scanner.allySlotConfirmedChampions[0] = yuumi
        scanner.enemySlotConfirmedChampions[1] = darius
        scanner.allyRolesBySlotFlow.value = mapOf(0 to com.example.model.LaneRole.SUPPORT)
        com.example.service.screen.LiteRTVisionClassifier.manuallyConfirmTenthPick(com.example.model.Champion(id = "vi", name = "Vi"))
        return com.example.service.OverlayState().apply {
            isExpanded = true
            autoScanEnabled = active
            allies[4] = yuumi
            enemies[0] = darius
            manualLockedAllySlots[4] = true
        }
    }

    private fun renderOverlay(state: com.example.service.OverlayState) {
        compose.setContent {
            com.example.ui.theme.MyApplicationTheme {
                com.example.service.FloatingOverlayContent(state, isLandscapeMode = false,
                    screenCaptureManager = null, onClose = {}, onDragDelta = { _, _, _, _ -> },
                    onExpandedChange = {}, onCompactModeChange = {})
            }
        }
    }

    private fun exerciseShortcuts(language: String, active: Boolean) {
        val state = overlayState(active)
        AppLanguage.select(RuntimeEnvironment.getApplication(), language)
        val report = com.example.service.screen.LiteRTVisionClassifier.reportFlow.value
        val teams = (state.allies + state.enemies).map { it?.id }
        renderOverlay(state)
        repeat(2) {
            compose.onNodeWithTag("btn_live_vision_toggle").assertIsDisplayed().performClick()
            compose.onNodeWithTag("btn_debug_overlay").assertIsDisplayed().performClick()
            compose.onNodeWithTag("vision_viewer_back").assertIsDisplayed().performClick()
            compose.runOnIdle {
                assertEquals(active, state.autoScanEnabled)
                assertEquals(teams, (state.allies + state.enemies).map { it?.id })
                assertTrue(state.manualLockedAllySlots[4] == true)
                assertEquals("yuumi", com.example.service.screen.DraftVisionScanner.allySlotConfirmedChampions[0]?.id)
                assertEquals(report, com.example.service.screen.LiteRTVisionClassifier.reportFlow.value)
            }
        }
    }

    @Test fun `back remains visible while the viewer content is rendered`() {
        compose.setContent {
            var viewerOpen by remember { mutableStateOf(true) }
            Box(Modifier.width(280.dp).height(400.dp)) {
                if (viewerOpen) LiteRTEngineViewerDialog(onDismissRequest = { viewerOpen = false }, initializeDiagnostics = false) else Text("Hub")
            }
        }
        compose.onNodeWithTag("vision_viewer_back").assertIsDisplayed()
        compose.onNodeWithText("CALIBRACIÓN DE UMBRAL DE SIMILITUD").assertDoesNotExist()
        compose.onNodeWithTag("vision_viewer_back").assertIsDisplayed().performClick()
        compose.onNodeWithText("Hub").assertIsDisplayed()
        compose.onNodeWithTag("litert_viewer_dialog").assertDoesNotExist()
    }

    @Test fun `Portuguese back action remains available with enlarged text`() {
        AppLanguage.select(RuntimeEnvironment.getApplication(), "pt")
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 1.5f)) {
                Box(Modifier.width(280.dp).height(400.dp)) { LiteRTEngineViewerDialog(onDismissRequest = {}, initializeDiagnostics = false) }
            }
        }
        compose.onNodeWithText("Voltar ao hub").assertIsDisplayed()
        compose.onNodeWithText("Voltar ao hub").assertIsDisplayed()
    }
}
