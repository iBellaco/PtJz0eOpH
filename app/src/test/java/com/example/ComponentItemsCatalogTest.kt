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
class ComponentItemsCatalogTest(private val id: String) {
    companion object {
        private fun expectations(): JSONObject {
            val asset = File("src/main/assets/wild_rift_component_items.json").takeIf { it.exists() }
                ?: File("app/src/main/assets/wild_rift_component_items.json")
            val sections = JSONObject(asset.readText()).getJSONObject("secciones")
            val result = JSONObject()
            sections.keys().forEach { section ->
                val levels = sections.getJSONObject(section)
                levels.keys().forEach { level ->
                    val entries = levels.getJSONArray(level)
                    for (i in 0 until entries.length()) {
                        val entry = entries.getJSONObject(i)
                        fun rows(key: String) = entry.getJSONArray(key).let { array -> (0 until array.length()).joinToString(" • ") { array.getString(it) } }
                        fun effects(suffix: String) = entry.getJSONArray("efectos").let { array -> (0 until array.length()).joinToString("\n") {
                            val effect = array.getJSONObject(it)
                            effect.getString("nombre$suffix") + ": " + effect.getString("descripcion$suffix")
                        } }
                        result.put(entry.getString("id"), JSONObject().put("goldCost",entry.getInt("coste_oro")).put("category",level)
                            .put("name",entry.getString("nombre")).put("namePt",entry.getString("nombre_pt"))
                            .put("stats",rows("estadisticas")).put("statsPt",rows("estadisticas_pt"))
                            .put("passive",effects("")).put("passivePt",effects("_pt")))
                    }
                }
            }
            return result
        }
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun items() = expectations().keys().asSequence().sorted().map { arrayOf(it) }.toList()
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
        stats.split(" • ").filter { it.isNotBlank() }.forEach { compose.onNodeWithText(it, useUnmergedTree = true).assertExists() }
        assertEquals(if (language == "pt") expected.getString("namePt") else expected.getString("name"), item.getLocalizedName(language))
        assertEquals(stats, item.getLocalizedStats(language))
        assertEquals(passive, item.getLocalizedPassive(language))
        if (passive.isNotBlank()) compose.onNodeWithText(passive, useUnmergedTree = true).assertExists()
        if (passive.isNotBlank()) {
            val rendered = compose.onNodeWithText(passive, useUnmergedTree = true).fetchSemanticsNode().config[SemanticsProperties.Text].single()
            assertEquals(com.example.ui.components.formatWildRiftDescription(passive).spanStyles, rendered.spanStyles)
            assertEquals(passive, rendered.text)
        }
        val visible = texts()
        if (language == "pt") assertFalse("Spanish in $id: $visible", visible.any { SpanishUiResidue.pattern.containsMatchIn(it) })
        else assertFalse("Portuguese in $id: $visible", visible.any {
            Regex("\\b(?:você|não|habilidade|dano|campeões|velocidade|adicionais|inimigos|acertos)\\b", RegexOption.IGNORE_CASE).containsMatchIn(it)
        })
        File(output, "component-item-$id-$language.json").writeText(JSONArray(visible).toString(2))
        if (id in setOf("tear_of_the_goddess", "quicksilver_sash", "bramble_vest", "mejais_soulstealer")) {
            if (passive.isNotBlank()) compose.onNodeWithText(passive, useUnmergedTree = true).performScrollTo()
            compose.onAllNodes(isRoot(), useUnmergedTree = true).onLast()
                .captureRoboImage(filePath = File(output, "component-item-$id-$language.png").path)
        }
    }
    @Test fun `required balance survives actual catalog rendering and both language switches`() {
        val item = WildRiftItemsData.getItemById(id)!!
        val expected = expectations().getJSONObject(id)
        if (expected.has("goldCost")) assertEquals(expected.getInt("goldCost"), item.goldCost)
        if (expected.has("category")) assertEquals(expected.getString("category"), item.category)
        assertEquals(id, WildRiftItemsData.getItemByName(expected.getString("name"))?.id)
        assertEquals(id, WildRiftItemsData.getItemByName(expected.getString("namePt"))?.id)
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
