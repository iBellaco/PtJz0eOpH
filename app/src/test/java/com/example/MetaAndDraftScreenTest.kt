package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.ui.screens.MetaAndDraftScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.model.LaneRole
import com.example.util.LocalLanguage
import androidx.compose.runtime.CompositionLocalProvider

@RunWith(AndroidJUnit4::class)
class MetaAndDraftScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testClickRunesTab() {
        composeTestRule.setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLanguage provides "es") {
                    MetaAndDraftScreen(
                        onNavigateBack = {},
                        userMainRole = LaneRole.MID
                    )
                }
            }
        }
        
        // Clic en la pestaña Runas
        composeTestRule.onNodeWithText("Runas").performClick()
        composeTestRule.waitForIdle()
    }
}
