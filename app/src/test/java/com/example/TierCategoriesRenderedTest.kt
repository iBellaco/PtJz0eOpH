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
                compose.onNodeWithText(title, substring = true).performScrollTo().assertIsDisplayed()
            }
        } finally {
            WildRiftRepository.champions.clear()
            WildRiftRepository.champions.addAll(original)
        }
    }
}
