package com.example

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.components.AnimatedLogoutButton
import com.example.ui.components.ThemeCustomizationBottomSheet
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.CoachNavStyle
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationCustomizationRenderedTest {
    @get:Rule val compose = createComposeRule()

    @Test fun premiumUserPreviewsAndNavigatesBeforeApplyingStyle() {
        val context = RuntimeEnvironment.getApplication()
        com.example.util.DynamicTranslations.loadSync(context)
        AppThemeManager.setNavStyle(CoachNavStyle.ORIGINAL)
        compose.setContent { MyApplicationTheme { ThemeCustomizationBottomSheet(isPremium=true, onDismiss={}) } }
        compose.onNodeWithTag("theme_nav_style_liquid").performScrollTo().performClick()
        assertEquals(CoachNavStyle.ORIGINAL, AppThemeManager.currentNavStyle)
        compose.onNodeWithTag("theme_nav_preview").performScrollTo().assertExists()
        compose.onNodeWithTag("coach_nav_item_3").performClick()
        assertEquals(CoachNavStyle.ORIGINAL, AppThemeManager.currentNavStyle)
        compose.onNodeWithTag("theme_nav_apply").performScrollTo().performClick()
        assertEquals(CoachNavStyle.LIQUID, AppThemeManager.currentNavStyle)
        AppThemeManager.setNavStyle(CoachNavStyle.ORIGINAL)
    }

    @Test fun nonPremiumUserCannotPreviewOrApplyPaidStyles() {
        AppThemeManager.setNavStyle(CoachNavStyle.ORIGINAL)
        compose.setContent { MyApplicationTheme { ThemeCustomizationBottomSheet(isPremium=false, onDismiss={}) } }
        compose.onNodeWithTag("theme_nav_preview").assertDoesNotExist()
        compose.onNodeWithTag("theme_nav_apply").assertDoesNotExist()
        compose.onNodeWithTag("theme_nav_style_liquid").performScrollTo().performClick()
        assertEquals(CoachNavStyle.ORIGINAL, AppThemeManager.currentNavStyle)
    }

    @Test fun realLogoutButtonRequestsConfirmationBeforeAnimationOrSignOut() {
        var requests = 0
        var logouts = 0
        compose.setContent { MyApplicationTheme {
            AnimatedLogoutButton(onRequestConfirmation={ requests++ }, onLogoutComplete={ logouts++ })
        } }
        compose.onNodeWithTag("animated_logout_button").performClick()
        compose.waitForIdle()
        assertEquals(1, requests)
        assertEquals(0, logouts)
    }
}
