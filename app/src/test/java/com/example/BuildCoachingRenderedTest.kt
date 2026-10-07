package com.example

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.WildRiftRepository
import com.example.data.local.CustomChampionBuildsManager
import com.example.data.local.FavoriteChampionsManager
import com.example.ui.screens.ChampionDetailSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@org.robolectric.annotation.SQLiteMode(org.robolectric.annotation.SQLiteMode.Mode.NATIVE)
class BuildCoachingRenderedTest {
    @get:Rule val compose=createComposeRule()
    @Test fun `Syndra build has a personal plan and Flash has no coach card in Spanish`()=inspect("es")
    @Test fun `Syndra build and element decisions are localized in Portuguese`()=inspect("pt")
    @Test fun `Vi boot selections keep their own tier two and tier three advice in Spanish`()=inspectBoots("es")
    @Test fun `Vi boot selections keep their own tier two and tier three advice in Portuguese`()=inspectBoots("pt")

    private fun inspectBoots(language: String) {
        val context = RuntimeEnvironment.getApplication()
        com.example.util.DynamicTranslations.loadSync(context)
        AppLanguage.select(context, language)
        WildRiftRepository.initChampions(context, forceReload = true)
        FavoriteChampionsManager.init(context)
        CustomChampionBuildsManager.init(context)
        val field = CustomChampionBuildsManager::class.java.getDeclaredField("_customBuilds").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val records = field.get(CustomChampionBuildsManager) as kotlinx.coroutines.flow.MutableStateFlow<List<com.example.data.local.CustomChampionBuildRecord>>
        records.value = CustomChampionBuildsManager.getDefaultBuilds(context)
        val vi = WildRiftRepository.champions.first { it.id == "vi" }
        compose.setContent { MyApplicationTheme { ChampionDetailSheet(champion = vi, isOverlay = false, onDismiss = {}) } }
        val close = if (language == "pt") "Fechar" else "Cerrar"
        fun checkAdvice(name: String, unwanted: String) {
            val localized = com.example.data.WildRiftItemsData.getItemByName(name)!!.getLocalizedName(language)
            val other = com.example.data.WildRiftItemsData.getItemByName(unwanted)!!.getLocalizedName(language)
            compose.onNode(hasText(localized, substring = true) and hasAnyAncestor(hasTestTag("build_element_advice_card"))).assertExists()
            compose.onAllNodes(hasText(other, substring = true) and hasAnyAncestor(hasTestTag("build_element_advice_card"))).assertCountEquals(0)
            val out = File("build/reports/portuguese-rendered").apply { mkdirs() }
            compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(out, "build-coaching-vi-${name.replace(' ', '_')}-$language.png").path)
            compose.onNodeWithText(close).performClick()
        }
        compose.onNodeWithTag("selected_boot_Grebas codiciosas").performScrollTo().performClick()
        checkAdvice("Grebas codiciosas", "Botas de mercurio")
        compose.onNodeWithTag("build_boot_Botas de mercurio").performScrollTo().performClick()
        compose.onNodeWithText(close).performClick()
        compose.onNodeWithTag("selected_boot_Botas de mercurio").performScrollTo().performClick()
        checkAdvice("Botas de mercurio", "Grebas codiciosas")
        compose.onNodeWithTag("selected_boot_Trituradoras encadenadas").performScrollTo().performClick()
        checkAdvice("Trituradoras encadenadas", "Botas inmortales")
        compose.onNodeWithTag("build_boot_Grebas codiciosas").performScrollTo().performClick()
        compose.onNodeWithText(close).performClick()
        compose.onNodeWithTag("selected_boot_Botas inmortales").performScrollTo().performClick()
        checkAdvice("Botas inmortales", "Trituradoras encadenadas")
    }

    private fun inspect(language:String) {
        val context=RuntimeEnvironment.getApplication()
        com.example.util.DynamicTranslations.loadSync(context)
        AppLanguage.select(context,language)
        WildRiftRepository.initChampions(context,forceReload=true)
        FavoriteChampionsManager.init(context)
        CustomChampionBuildsManager.init(context)
        val field=CustomChampionBuildsManager::class.java.getDeclaredField("_customBuilds").apply { isAccessible=true }
        @Suppress("UNCHECKED_CAST")
        val records=field.get(CustomChampionBuildsManager) as kotlinx.coroutines.flow.MutableStateFlow<List<com.example.data.local.CustomChampionBuildRecord>>
        records.value=CustomChampionBuildsManager.getDefaultBuilds(context)
        val syndra=WildRiftRepository.champions.first { it.id=="syndra" }
        compose.setContent { MyApplicationTheme { ChampionDetailSheet(champion=syndra,isOverlay=false,onDismiss={}) } }
        compose.onNodeWithTag("build_coach_overview").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(if(language=="pt") "CRITÉRIO COACH" else "CRITERIO COACH").assertExists()
        compose.onAllNodesWithText("ESTADÍSTICA & IA").assertCountEquals(0)
        val out=File("build/reports/portuguese-rendered").apply { mkdirs() }
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(out,"build-coaching-overview-$language.png").path)
        compose.onAllNodesWithTag("build_item_details").onFirst().performScrollTo().performClick()
        compose.onNodeWithTag("build_element_advice_card").assertExists()
        for (placeholder in listOf("Composiciones rivales especializadas", "Amenazas prioritarias de la partida",
            "Composições adversárias especializadas", "Ameaças prioritárias do jogo")) {
            compose.onAllNodesWithText(placeholder, substring = true).assertCountEquals(0)
        }
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(out,"build-coaching-core-$language.png").path)
        compose.onNodeWithText(if(language=="pt") "Fechar" else "Cerrar").performClick()
        compose.onAllNodesWithTag("build_rune_details").onFirst().performScrollTo().performClick()
        compose.onNodeWithTag("build_element_advice_card").assertExists()
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(out,"build-coaching-rune-$language.png").path)
        compose.onNodeWithText(if(language=="pt") "Fechar" else "Cerrar").performClick()
        compose.onAllNodesWithTag("build_spell_details").onFirst().performScrollTo().performClick()
        compose.onNodeWithText(if(language=="pt") "Flash" else "Destello").assertExists()
        compose.onAllNodesWithTag("build_element_advice_card").assertCountEquals(0)
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(out,"build-coaching-flash-$language.png").path)
    }
}
