package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.data.WildRiftSpellsAndRunes
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
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
    @Test fun `Spanish map labels appear only inside the spell description`() = inspect("es")
    @Test fun `Portuguese map labels appear only inside the spell description`() = inspect("pt")

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
        fun checkCompactCatalog() {
            compose.onAllNodesWithTag("spell_map_labels", useUnmergedTree = true).assertCountEquals(0)
            compose.onAllNodesWithText(abyss, substring = true, useUnmergedTree = true).assertCountEquals(0)
            compose.onNode(hasText("100s", substring = true) and hasAnyAncestor(hasTestTag("catalog_spell_barrier")), useUnmergedTree = true).assertIsDisplayed()
            compose.onAllNodesWithText("Mapas aplicables", substring = true).assertCountEquals(0)
            compose.onAllNodesWithText("Mapas aplicáveis", substring = true).assertCountEquals(0)
            compose.onAllNodesWithText("Howling Abyss", substring = true).assertCountEquals(0)
        }
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(output, "spell-catalog-grid-$language.png").path)
        compose.onNodeWithTag("catalog_spell_barrier").assertHeightIsAtLeast(112.dp)
        org.junit.Assert.assertTrue("Spell grid should not retain the old empty 184dp card",
            compose.onNodeWithTag("catalog_spell_barrier").getUnclippedBoundsInRoot().height < 140.dp)
        checkCompactCatalog()
        compose.onNodeWithTag("catalog_spell_barrier").performClick()
        // The grid remains behind the dialog; assert the dialog's own label.
        compose.onNode(hasText(abyss) and hasAnyAncestor(isDialog()), useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText(if (language == "pt") "Concede um escudo" else "Otorga un escudo", substring = true).assertExists()
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(output, "spell-catalog-dialog-$language.png").path)
        compose.onNodeWithText(close).performClick()
        checkCompactCatalog()
        compose.onNodeWithText(if (language == "pt") "Detalhado" else "Detallado").performClick()
        checkCompactCatalog()
        compose.onNodeWithText(if (language == "pt") "Concede um escudo" else "Otorga un escudo", substring = true).assertExists()
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(output, "spell-catalog-list-$language.png").path)
        compose.onNodeWithTag("catalog_spell_barrier").performClick()
        compose.onNode(hasText(abyss) and hasAnyAncestor(isDialog()), useUnmergedTree = true).assertIsDisplayed()
    }
}
