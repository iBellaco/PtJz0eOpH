package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.ui.components.MatchupPreviewDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.util.LocalLanguage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class LiveMatchupStatsTest {
    @get:Rule val compose = createComposeRule()
    private var original = emptyList<Champion>()
    private val own = Champion(id = "ahri", name = "Ahri", hasRegionalStats = true, winrate = 52.41)
    private val enemy = Champion(id = "akali", name = "Akali", hasRegionalStats = true, winrate = 49.12)
    @Before fun prepare() {
        original = WildRiftRepository.champions.toList()
        WildRiftRepository.champions.clear()
        WildRiftRepository.champions.addAll(listOf(own, enemy))
    }
    @After fun restore() {
        WildRiftRepository.champions.clear()
        WildRiftRepository.champions.addAll(original)
    }
    @Test fun openPreviewReceivesNewRatesEvenWhenItsOriginalChampionParameterDoesNotChange() {
        compose.setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLanguage provides "es") {
                    MatchupPreviewDialog(isOverlay = true, myChampion = own,
                        enemyOpponent = enemy, activeRole = LaneRole.MID, onDismiss = {})
                }
            }
        }
        compose.onNodeWithText("WR: 52.41%").assertExists()
        compose.runOnIdle { WildRiftRepository.champions[0] = own.copy(winrate = 54.72) }
        compose.onNodeWithText("WR: 54.72%").assertExists()
        compose.onNodeWithText("WR: 52.41%").assertDoesNotExist()
    }
}
