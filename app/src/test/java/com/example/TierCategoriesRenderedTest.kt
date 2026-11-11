package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.WildRiftRepository
import com.example.ui.screens.TierListTab
import com.example.ui.theme.MyApplicationTheme
import com.example.util.LocalLanguage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TierCategoriesRenderedTest {
    @get:Rule val compose = createComposeRule()

    @Test fun allFiveCategoriesAreReachableAndThereIsNoExportButton() {
        WildRiftRepository.initChampions(RuntimeEnvironment.getApplication(), forceReload = true)
        val original = WildRiftRepository.champions.toList()
        val tiers = listOf("S+", "S", "A", "B", "C")
        WildRiftRepository.champions.clear()
        WildRiftRepository.champions.addAll(original.take(5).mapIndexed { index, champion -> champion.copy(tier = tiers[index]) })
        try {
            compose.setContent { MyApplicationTheme {
                CompositionLocalProvider(LocalLanguage provides "es") { TierListTab(onSelectChampion = {}) }
            } }
            compose.onNodeWithTag("tier_list_export").assertDoesNotExist()
            compose.onNodeWithTag("tier_list_export_button").assertDoesNotExist()
            tiers.forEach { tier ->
                val title = "TIER $tier" + if (tier in listOf("S+", "S", "A")) " (" else ""
                compose.onNodeWithTag("tier_list").performScrollToNode(hasText(title, substring = true))
                compose.onNodeWithText(title, substring = true).assertIsDisplayed()
            }
        } finally {
            WildRiftRepository.champions.clear()
            WildRiftRepository.champions.addAll(original)
        }
    }

    @Test fun championsWithoutCompetitiveDataAreNotShownInTheTierList() {
        WildRiftRepository.initChampions(RuntimeEnvironment.getApplication(), forceReload = true)
        val original = WildRiftRepository.champions.toList()
        val withData = original.first().copy(name = "Con datos", tier = "S")
        val withoutData = original.first().copy(id = "sylas", name = "Sylas", tier = "SIN DATOS")
        WildRiftRepository.champions.clear()
        WildRiftRepository.champions.addAll(listOf(withData, withoutData))
        try {
            compose.setContent { MyApplicationTheme {
                CompositionLocalProvider(LocalLanguage provides "es") { TierListTab(onSelectChampion = {}) }
            } }
            compose.onNodeWithTag("tier_list").performScrollToNode(hasText("Con datos", substring = true))
            compose.onNodeWithText("Con datos", substring = true).assertIsDisplayed()
            compose.onNodeWithText("Sylas", substring = true).assertDoesNotExist()
            compose.onNodeWithText("Nuevo: sin datos competitivos", substring = true).assertDoesNotExist()
        } finally {
            WildRiftRepository.champions.clear()
            WildRiftRepository.champions.addAll(original)
        }
    }
}
