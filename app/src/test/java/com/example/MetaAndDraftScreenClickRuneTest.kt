package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.ui.screens.MetaAndDraftScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.model.LaneRole
import com.example.util.LocalLanguage
import androidx.compose.runtime.CompositionLocalProvider
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
class MetaAndDraftScreenClickRuneTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testClickRuneItem() {
        ShadowLog.stream = System.out
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
        
        composeTestRule.onNodeWithText("Runas").performClick()
        composeTestRule.waitForIdle()
        
        // Clic en la runa "Electrocutar"
        composeTestRule.onNodeWithText("Electrocutar").performClick()
        composeTestRule.waitForIdle()
    }
}
