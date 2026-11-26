package com.example

import android.app.Application
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import com.example.ui.components.ThemeCustomizationBottomSheet
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.MyApplicationTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ApplicationThemeSelectionTest {
    @get:Rule val compose = createComposeRule()
    private val originalTheme = AppThemeManager.currentTheme

    @After fun restoreTheme() {
        AppThemeManager.setTheme(originalTheme, RuntimeEnvironment.getApplication())
    }

    @Test fun previewThenApplyUpdatesTheDisplayedColorsAndRestoresTheSavedChoice() {
        val context = RuntimeEnvironment.getApplication()
        AppThemeManager.setTheme(AppTheme.PILTOVER, context)
        lateinit var displayed: ColorScheme
        compose.setContent {
            MyApplicationTheme {
                val colors = MaterialTheme.colorScheme
                SideEffect { displayed = colors }
                ThemeCustomizationBottomSheet(isPremium = true, onDismiss = {})
            }
        }
        val themes = AppTheme.entries.filter { it.isApplicationTheme }
        for (theme in listOf(AppTheme.EMBER, AppTheme.MINT)) {
            val previous = AppThemeManager.currentTheme
            compose.onNodeWithTag("theme_application_carousel").performScrollTo().performScrollToIndex(themes.indexOf(theme))
            compose.onNodeWithTag("theme_application_${theme.id.lowercase()}").performClick()
            compose.runOnIdle { assertEquals(previous, AppThemeManager.currentTheme) }
            compose.onNodeWithTag("theme_apply_button").performScrollTo().performClick()
            compose.runOnIdle {
                assertEquals(theme, AppThemeManager.currentTheme)
                assertEquals(theme.primary, displayed.primary)
                assertEquals(theme.secondary, displayed.secondary)
                assertEquals(theme.surface, displayed.surfaceContainer)
                assertEquals(theme.surfaceVariant, displayed.surfaceContainerHighest)
                assertEquals(theme.primaryGlow, displayed.tertiary)
            }
        }
        compose.runOnIdle {
            AppThemeManager.setTheme(AppTheme.PILTOVER)
            AppThemeManager.init(context)
            assertEquals(AppTheme.MINT, AppThemeManager.currentTheme)
        }
    }
}
