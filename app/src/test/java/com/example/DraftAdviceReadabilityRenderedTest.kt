package com.example

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w320dp-h1000dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftAdviceReadabilityRenderedTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fullWidthAdviceKeepsItsLastSentenceWithLargeTextInBothLanguages() {
        val context = RuntimeEnvironment.getApplication()
        DynamicTranslations.loadSync(context)
        var advice by mutableStateOf("")
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.8f)) {
                MyApplicationTheme {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                        DraftAdviceSection("Sinergia / Combo:", advice, HextechCyan, "full_advice")
                    }
                }
            }
        }
        try {
            for (language in listOf("es", "pt")) {
                val sentence = if (language == "es") "Controla la zona con tus aliados." else "Controle a zona com seus aliados."
                val ending = if (language == "es") "Conserva H4 para la entrada rival." else "Guarde H4 para a entrada inimiga."
                // Distinct instructions avoid measuring the duplicate-sentence cleanup instead of layout.
                compose.runOnIdle {
                    AppLanguage.select(context, language)
                    advice = (1..12).joinToString(" ") { "$it: $sentence" } + " " + ending
                }
                val results = mutableListOf<TextLayoutResult>()
                compose.onNodeWithTag("full_advice_text", useUnmergedTree = true)
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                val layout = results.single()
                assertFalse("$language must not truncate long advice", layout.hasVisualOverflow)
                assertTrue(layout.lineCount > 10)
                assertTrue(layout.layoutInput.text.text.endsWith(ending))
            }
        } finally { AppLanguage.select(context, "es") }
    }

    @OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
    @Test fun referenceBarsKeepLabelsAndSelectionAt320dpWithLargeText() {
        val context = RuntimeEnvironment.getApplication()
        DynamicTranslations.loadSync(context)
        var style by mutableStateOf(CoachNavStyle.GLASS_PILL)
        var selected by mutableIntStateOf(0)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.8f)) {
                MyApplicationTheme {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                        VideoCoachNavBar(CoachNavPreviewItems(), selected, style, { selected = it })
                    }
                }
            }
        }
        try {
            for (language in listOf("es", "pt")) for (option in listOf(CoachNavStyle.GLASS_PILL,
                CoachNavStyle.TABS_V2, CoachNavStyle.PURPLE_MESSAGES, CoachNavStyle.MENISCUS)) {
                compose.runOnIdle { AppLanguage.select(context, language); style = option }
                for (i in 0..4) {
                    compose.onNodeWithTag("coach_nav_item_$i").performClick().assertIsSelected()
                    val results = mutableListOf<TextLayoutResult>()
                    compose.onNodeWithTag("nav_label_$i", useUnmergedTree = true)
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    assertFalse("$language/$option/$i", results.single().hasVisualOverflow)
                    val bounds = compose.onNodeWithTag("coach_nav_item_$i").fetchSemanticsNode().boundsInRoot
                    assertTrue(bounds.width >= 48f && bounds.height >= 48f)
                }
                val output = java.io.File("build/reports/portuguese-rendered/nav-reference-${option.id}-$language.png")
                output.parentFile.mkdirs()
                compose.onRoot().captureRoboImage(output.path, roborazziOptions =
                    com.github.takahirom.roborazzi.RoborazziOptions(taskType = com.github.takahirom.roborazzi.RoborazziTaskType.Record))
            }
        } finally { AppLanguage.select(context, "es") }
    }
}
