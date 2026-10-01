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

    @Test fun `back remains visible while the viewer content is rendered`() {
        compose.setContent {
            var viewerOpen by remember { mutableStateOf(true) }
            Box(Modifier.width(280.dp).height(400.dp)) {
                if (viewerOpen) LiteRTEngineViewerDialog(initializeDiagnostics = false) { viewerOpen = false } else Text("Hub")
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
                Box(Modifier.width(280.dp).height(400.dp)) { LiteRTEngineViewerDialog(initializeDiagnostics = false) {} }
            }
        }
        compose.onNodeWithText("Voltar ao hub").assertIsDisplayed()
        compose.onNodeWithText("Voltar ao hub").assertIsDisplayed()
    }
}
