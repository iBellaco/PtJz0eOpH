package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.ui.screens.MetaAndDraftScreen
import com.example.ui.screens.MetaScreenMode
import com.example.ui.theme.MyApplicationTheme
import com.example.model.LaneRole
import com.example.util.LocalLanguage
import androidx.compose.runtime.CompositionLocalProvider
import org.robolectric.shadows.ShadowLog
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
@SQLiteMode(SQLiteMode.Mode.LEGACY)
class FullAppFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAllCatalogTabsAndInteractions() {
        ShadowLog.stream = System.out
        composeTestRule.setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLanguage provides "es") {
                    MetaAndDraftScreen(
                        mode = MetaScreenMode.CATALOG,
                        onNavigateBack = {},
                        userMainRole = LaneRole.MID
                    )
                }
            }
        }
        
        // 1. Initial tab check
        composeTestRule.waitForIdle()
        
        // 2. Tab Objetos
        composeTestRule.onNodeWithText("Objetos").performClick()
        composeTestRule.waitForIdle()

        // 3. Tab Runas
        composeTestRule.onNodeWithText("Runas").performClick()
        composeTestRule.waitForIdle()

        // Test view switch to Detallado
        composeTestRule.onNodeWithText("Detallado").performClick()
        composeTestRule.waitForIdle()

        // Test view switch back to Cuadrícula
        composeTestRule.onNodeWithText("Cuadrícula").performClick()
        composeTestRule.waitForIdle()

        // Test filter chip Clave
        composeTestRule.onNodeWithText("Clave").performClick()
        composeTestRule.waitForIdle()

        // Test filter chip Todos
        composeTestRule.onNodeWithText("Todos").performClick()
        composeTestRule.waitForIdle()

        // 5. Tab Hechizos
        composeTestRule.onNodeWithText("Hechizos").performClick()
        composeTestRule.waitForIdle()

        // Return to Objetos
        composeTestRule.onNodeWithText("Objetos").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun testDraftingModeScreen() {
        ShadowLog.stream = System.out
        composeTestRule.setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLanguage provides "es") {
                    MetaAndDraftScreen(
                        mode = MetaScreenMode.DRAFTING,
                        onNavigateBack = {},
                        userMainRole = LaneRole.TOP
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testMainActivityDraftingApp() {
        ShadowLog.stream = System.out
        composeTestRule.setContent {
            DraftingApp()
        }
        composeTestRule.waitForIdle()
    }
}
