package com.example

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.ui.components.CalibrationControls
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w840dp-h1600dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CalibrationControlsLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun controlsStayTouchableAndDoNotOverlapAcrossWidthsFontsAndLanguages() {
        val context = RuntimeEnvironment.getApplication()
        var width by mutableStateOf(320)
        var font by mutableStateOf(1f)
        var language by mutableStateOf("es")
        var step by mutableStateOf(.005f)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, font)) {
                MyApplicationTheme {
                    Box(Modifier.width(width.dp).padding(12.dp).verticalScroll(rememberScrollState())) {
                        CalibrationControls(step, { step = it }, { _,_,_ -> }, {}, {})
                    }
                }
            }
        }
        try {
            for (lang in listOf("es", "pt")) for (w in listOf(280,320,411,600)) for (scale in listOf(1f,1.3f,1.6f)) {
                compose.runOnIdle { language = lang; AppLanguage.select(context, language); width = w; font = scale }
                compose.waitForIdle()
                val tags = listOf("vision_calibration_up", "vision_calibration_left", "vision_calibration_right", "vision_calibration_down",
                    "calibration_size_increase", "calibration_size_decrease", "calibration_step_0_1p", "calibration_step_0_5p", "calibration_step_1_0p", "calibration_step_2_0p", "vision_calibration_reset_device")
                for (tag in tags) {
                    val node = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
                    node.assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
                    node.performClick()
                }
                compose.onNodeWithTag("vision_calibration_up").performScrollTo()
                val bounds = tags.map { compose.onNodeWithTag(it).fetchSemanticsNode().boundsInRoot }
                for (i in bounds.indices) for (j in i+1 until bounds.size) {
                    assertFalse("Overlapping controls at $lang $w/$scale: ${tags[i]}, ${tags[j]}",
                        bounds[i].overlaps(bounds[j]))
                }
                for (text in listOf(com.example.util.appTr("Agrandar"), com.example.util.appTr("Reducir"))) {
                    val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
                    compose.onNodeWithText(text, useUnmergedTree = true).performSemanticsAction(
                        androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult
                    ) { it(layouts) }
                    assertEquals("Size label wraps at $lang $w/$scale", 1, layouts.single().lineCount)
                    assertFalse(layouts.single().hasVisualOverflow)
                }
            }
        } finally { AppLanguage.select(context, "es") }
    }
}
