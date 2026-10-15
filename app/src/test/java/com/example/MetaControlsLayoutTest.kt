package com.example

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.ui.components.AdminDashboardHeader
import com.example.ui.components.RateChangeBadge
import com.example.ui.components.SparklineTrendGraph
import com.example.ui.theme.MyApplicationTheme
import com.example.util.LocalLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class MetaControlsLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun allManagementActionsWrapWithinNarrowScreenWithoutHorizontalScrolling() {
        compose.setContent {
            MyApplicationTheme { CompositionLocalProvider(LocalLanguage provides "es") {
                Box(Modifier.requiredSize(320.dp, 640.dp)) {
                    AdminDashboardHeader(onClose = {}, onOpenUsers = {}, onOpenFeedbackAndSupport = {},
                        onOpenBroadcast = {}, onOpenNotice = {})
                }
            } }
        }
        val grid = compose.onNodeWithTag("admin_action_grid").fetchSemanticsNode().boundsInRoot
        val tags = listOf("open_user_directory", "open_admin_support", "open_admin_sponsor_panel",
            "open_admin_moderation", "open_admin_broadcast", "open_admin_notice", "open_admin_cpm",
            "open_admin_database", "open_moderator_activity")
        val positions = tags.map { tag ->
            compose.onNodeWithTag(tag).assertIsDisplayed()
            compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.also {
                assertTrue("$tag outside left", it.left >= grid.left - 1f)
                assertTrue("$tag outside right", it.right <= grid.right + 1f)
            }
        }
        assertTrue("Actions must occupy several rows", positions.map { it.top.toInt() }.distinct().size > 1)
    }

    @Test fun graphShowsThreeRealValuesAndIdentifiesDailyReferences() {
        compose.setContent { MyApplicationTheme { CompositionLocalProvider(LocalLanguage provides "es") {
            SparklineTrendGraph(53.0, 1.0, observed24h = 52.0, observed12h = 52.5, dailyReferences = true)
        } } }
        listOf("hace 24h", "hace 12h", "actual", "52.00%", "52.50%", "53.00%", "Referencias diarias publicadas")
            .forEach { compose.onNodeWithText(it).assertExists() }
    }

    @Test fun unchangedRateIsNeutralRatherThanFalsePositiveZero() {
        compose.setContent { MyApplicationTheme { CompositionLocalProvider(LocalLanguage provides "es") { RateChangeBadge(0.0) } } }
        compose.onNodeWithText("= · 24h").assertExists()
        compose.onNodeWithText("+0.00%", substring = true).assertDoesNotExist()
    }
}
