package com.example

import android.app.Application
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.WildRiftItemsData
import com.example.ui.screens.selectedRuneItemModal
import com.example.ui.theme.MyApplicationTheme
import com.example.util.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** User-supplied balance corrections must survive rendering and ES/PT/ES switches. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RequiredItemCorrectionsTest(private val id: String) {
    companion object {
        private fun expectations() = JSONObject(RequiredItemCorrectionsTest::class.java
            .getResourceAsStream("/item-corrections-158.json")!!.bufferedReader().use { it.readText() })
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun items() = expectations().keys().asSequence().map { arrayOf(it) }.toList()
    }
    @get:Rule val compose = createComposeRule()
    private val context get() = RuntimeEnvironment.getApplication()
    private val output = File("build/reports/portuguese-rendered").apply { mkdirs() }
    @Before fun prepare() {
        DynamicTranslations.loadSync(context)
        AppLanguage.select(context, "es-419")
    }
    private fun texts(): List<String> = compose.onAllNodes(isRoot(), useUnmergedTree = true)
        .fetchSemanticsNodes().flatMap { root ->
            fun collect(node: androidx.compose.ui.semantics.SemanticsNode): List<String> =
                node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text } + node.children.flatMap(::collect)
            collect(root)
        }
    private fun verifyAndCapture(language: String, expected: JSONObject) {
        val item = WildRiftItemsData.getItemById(id)!!
        val passive = if (language == "pt") expected.optString("passivePt", item.getLocalizedPassive(language))
                      else expected.optString("passive", item.passive)
        val stats = if (language == "pt") expected.optString("statsPt", item.getLocalizedStats(language))
                    else expected.optString("stats", item.stats)
        compose.onNodeWithText(item.getLocalizedName(language), useUnmergedTree = true).assertExists()
        stats.split(" • ").forEach { compose.onNodeWithText(it, useUnmergedTree = true).assertExists() }
        compose.onNodeWithText(passive, useUnmergedTree = true).assertExists()
        val passiveNode = compose.onNodeWithText(passive, useUnmergedTree = true)
            .fetchSemanticsNode().config[SemanticsProperties.Text].single()
        // Confirm the actual rendered text still carries the existing semantic colours.
        val formatted = com.example.ui.components.formatWildRiftDescription(passive)
        assertEquals(formatted.spanStyles, passiveNode.spanStyles)
        assertEquals(passive, passiveNode.text)
        val visible = texts()
        if (language == "pt") assertFalse("Spanish in $id: $visible", visible.any { SpanishUiResidue.pattern.containsMatchIn(it) })
        else assertFalse("Portuguese in $id: $visible", visible.any {
            Regex("\\b(?:você|não|habilidade|habilidades|dano|campeões|velocidade|recarga|adicionais|inimigos|acertos|concede)\\b", RegexOption.IGNORE_CASE).containsMatchIn(it)
        })
        File(output, "required-item-$id-$language.json").writeText(JSONArray(visible).toString(2))
        if (language == "es" || language == "pt") {
            compose.onNodeWithText(passive, useUnmergedTree = true).performScrollTo()
            compose.onAllNodes(isRoot(), useUnmergedTree = true).onLast()
                .captureRoboImage(filePath = File(output, "required-item-$id-$language.png").path)
        }
    }
    @Test fun `required balance survives actual catalog rendering and both language switches`() {
        val item = WildRiftItemsData.getItemById(id)!!
        val expected = expectations().getJSONObject(id)
        if (expected.has("goldCost")) assertEquals(expected.getInt("goldCost"), item.goldCost)
        if (expected.has("category")) assertEquals(expected.getString("category"), item.category)
        compose.setContent { MyApplicationTheme { selectedRuneItemModal(item) {} } }
        verifyAndCapture("es", expected)
        compose.runOnIdle { AppLanguage.select(context, "pt-BR") }
        verifyAndCapture("pt", expected)
        compose.runOnIdle { AppLanguage.select(context, "es-419") }
        // Scroll position is retained but all text must return to Spanish immediately.
        verifyAndCapture("es", expected)
        assertEquals("es-419", java.util.Locale.getDefault().toLanguageTag())
    }
}
