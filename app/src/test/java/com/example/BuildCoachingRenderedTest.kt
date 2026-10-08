package com.example

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
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
    @Test fun `matchup avatars show their name without navigating in Spanish`()=inspectMatchupNames("es")
    @Test fun `matchup avatars show their name without navigating in Portuguese`()=inspectMatchupNames("pt")

    private fun inspectMatchupNames(language: String) {
        val context = RuntimeEnvironment.getApplication()
        com.example.util.DynamicTranslations.loadSync(context)
        AppLanguage.select(context, language)
        WildRiftRepository.initChampions(context, forceReload = true)
        FavoriteChampionsManager.init(context)
        CustomChampionBuildsManager.init(context)
        val champion = WildRiftRepository.champions.first { it.id == "syndra" }
        var navigations = 0
        @Suppress("UNCHECKED_CAST")
        fun <T> state(owner: Any, fieldName: String) = owner.javaClass.getDeclaredField(fieldName)
            .apply { isAccessible = true }.get(owner) as kotlinx.coroutines.flow.MutableStateFlow<T>
        val signedIn = state<Boolean>(com.example.util.AuthManager, "_isSignedIn")
        val premium = state<Boolean>(com.example.util.SubscriptionManager, "_isPremium")
        val role = state<String>(com.example.util.SubscriptionManager, "_userRole")
        val saved = Triple(signedIn.value, premium.value, role.value)
        try {
        role.value = "free"; signedIn.value = false; premium.value = false
        compose.setContent { MyApplicationTheme {
            ChampionDetailSheet(champion = champion, isOverlay = false, onDismiss = {},
                onChampionSelected = { navigations++ })
        } }
        for ((signed, pro, count) in listOf(Triple(false, false, 3), Triple(true, false, 6), Triple(true, true, 12))) {
            compose.runOnIdle { signedIn.value = signed; premium.value = pro }
            for (group in listOf("advantage", "weakness", "synergy")) {
                compose.onAllNodesWithTag("build_matchup_name_$group").assertCountEquals(count)
                for (row in 0 until count / 3) {
                    compose.onNodeWithTag("build_matchup_${group}_row_$row").assertExists()
                }
            }
            compose.onNodeWithTag("advantage_insight_card").performScrollTo()
            val cards = listOf("advantage", "weakness", "synergy").map {
                compose.onNodeWithTag("${it}_insight_card").fetchSemanticsNode()
            }
            org.junit.Assert.assertEquals(cards[0].positionInRoot.y, cards[1].positionInRoot.y, 1f)
            org.junit.Assert.assertEquals(cards[0].positionInRoot.y, cards[2].positionInRoot.y, 1f)
            org.junit.Assert.assertTrue(cards[0].positionInRoot.x < cards[1].positionInRoot.x)
            org.junit.Assert.assertTrue(cards[1].positionInRoot.x < cards[2].positionInRoot.x)
            val out = File("build/reports/portuguese-rendered").apply { mkdirs() }
            compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(out, "build-matchup-access-$count-$language.png").path)
        }
        for (group in listOf("advantage", "weakness", "synergy")) {
            val avatar = compose.onAllNodesWithTag("build_matchup_name_$group").onFirst().performScrollTo()
            val name = avatar.fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first()
            avatar.performSemanticsAction(SemanticsActions.OnClick) { it() }
            compose.onNodeWithTag("build_matchup_visible_name", useUnmergedTree = true).assertTextEquals(name).assertIsDisplayed()
            val out = File("build/reports/portuguese-rendered").apply { mkdirs() }
            compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(out, "build-matchup-name-$group-$language.png").path)
            org.junit.Assert.assertEquals(0, navigations)
            compose.onAllNodesWithText(if(language == "pt") "Fechar" else "Cerrar").assertCountEquals(0)
            compose.onNodeWithTag("build_matchup_name_popup").performClick()
            compose.onNodeWithTag("build_matchup_visible_name", useUnmergedTree = true).assertDoesNotExist()
        }
        } finally {
            signedIn.value = saved.first; premium.value = saved.second; role.value = saved.third
        }
    }

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
        val smiteName = WildRiftRepository.summonerSpells.first { it.name == "Aplastar" }.getLocalizedName(language)
        compose.onNode(hasTestTag("build_spell_details") and hasContentDescription(smiteName)).performScrollTo().performClick()
        compose.onNodeWithText(smiteName).assertExists()
        compose.onAllNodesWithTag("build_element_advice_card").assertCountEquals(0)
        val output = File("build/reports/portuguese-rendered").apply { mkdirs() }
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(output, "build-coaching-smite-$language.png").path)
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
        // The core row scrolls horizontally inside the vertically scrolling sheet.
        // Invoke the icon's click action without depending on its clipped center.
        compose.onAllNodesWithTag("build_item_details").onFirst().performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithTag("build_element_advice_card").assertExists()
        compose.onNode(hasText(if(language=="pt") "Quando completar:" else "Cuándo completarlo:", substring = true)
            and hasAnyAncestor(hasTestTag("build_element_advice_card"))).assertExists()
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
