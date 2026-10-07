package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.data.WildRiftSpellsAndRunes
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.screens.SpellsTab
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.After
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpellCatalogRenderedTest {
    @get:Rule val compose = createComposeRule()
    @After fun restoreCatalog() {
        WildRiftRepository.summonerSpells = WildRiftSpellsAndRunes.summonerSpells
    }
    @Test fun `Spanish catalog displays map labels in grid list and dialog`() = inspect("es")
    @Test fun `Portuguese catalog displays translated map labels in grid list and dialog`() = inspect("pt")

    private fun inspect(language: String) {
        val context = RuntimeEnvironment.getApplication()
        DynamicTranslations.loadSync(context)
        AppLanguage.select(context, language)
        // Isolate the card under test without opening a keyboard over its labels.
        WildRiftRepository.summonerSpells = WildRiftSpellsAndRunes.summonerSpells.filter { it.id == "barrier" }
        compose.setContent { MyApplicationTheme { SpellsTab() } }
        val abyss = if (language == "pt") "Abismo dos Lamentos" else "Abismo de los Lamentos"
        val close = if (language == "pt") "Fechar" else "Cerrar"
        val output = File("build/reports/portuguese-rendered").apply { mkdirs() }
        fun checkLabelsAndMechanics() {
            // Cards merge their accessible text; inspect the actual label child.
            compose.onNode(hasText(abyss) and hasAnyAncestor(hasTestTag("spell_map_labels")), useUnmergedTree = true).assertIsDisplayed()
            compose.onAllNodesWithText("Mapas aplicables", substring = true).assertCountEquals(0)
            compose.onAllNodesWithText("Mapas aplicáveis", substring = true).assertCountEquals(0)
            compose.onAllNodesWithText("Howling Abyss", substring = true).assertCountEquals(0)
        }
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(output, "spell-catalog-grid-$language.png").path)
        checkLabelsAndMechanics()
        compose.onNodeWithTag("catalog_spell_barrier").performClick()
        // The grid remains behind the dialog; assert the dialog's own label.
        compose.onNode(hasText(abyss) and hasAnyAncestor(isDialog()), useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText(if (language == "pt") "Concede um escudo" else "Otorga un escudo", substring = true).assertExists()
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(output, "spell-catalog-dialog-$language.png").path)
        compose.onNodeWithText(close).performClick()
        compose.onNodeWithText(if (language == "pt") "Detalhado" else "Detallado").performClick()
        checkLabelsAndMechanics()
        compose.onNodeWithText(if (language == "pt") "Concede um escudo" else "Otorga un escudo", substring = true).assertExists()
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(output, "spell-catalog-list-$language.png").path)
    }
}
