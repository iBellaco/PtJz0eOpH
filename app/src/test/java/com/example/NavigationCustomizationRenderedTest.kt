package com.example

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.Home
import com.github.takahirom.roborazzi.captureRoboImage
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

    @OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
    @Test fun liquidIndicatorIsCenteredOnTheSelectedIcon() {
        var density = 1f
        compose.setContent {
            density = androidx.compose.ui.platform.LocalDensity.current.density
            com.example.ui.components.LiquidNavBar(
                items = listOf(com.example.ui.components.CoachNavItem("Inicio",
                    androidx.compose.material.icons.Icons.Default.Home, androidx.compose.ui.graphics.Color.Red)),
                selectedIndex = 0, backgroundColor = androidx.compose.ui.graphics.Color.Black,
                selectedColor = androidx.compose.ui.graphics.Color.White, unselectedColor = androidx.compose.ui.graphics.Color.Gray,
                onSelected = {}, modifier = androidx.compose.ui.Modifier.testTag("liquid_alignment")
            )
        }
        compose.waitForIdle()
        val path = java.io.File("build/reports/portuguese-rendered/liquid-alignment-es.png")
        path.parentFile.mkdirs()
        compose.onNodeWithTag("liquid_alignment").captureRoboImage(
            path.path,
            roborazziOptions = com.github.takahirom.roborazzi.RoborazziOptions(
                taskType = com.github.takahirom.roborazzi.RoborazziTaskType.Record
            )
        )
        val bitmap = android.graphics.BitmapFactory.decodeFile(path.path)
        val x = (bitmap.width / 2 + 14 * density).toInt()
        val rows = (0 until bitmap.height).filter { y ->
            val pixel = bitmap.getPixel(x, y)
            android.graphics.Color.red(pixel) > 20 && android.graphics.Color.green(pixel) < 20 && android.graphics.Color.blue(pixel) < 20
        }
        assertTrue("Indicator must be visible", rows.isNotEmpty())
        val center = (rows.first() + rows.last()) / 2f
        assertTrue("Indicator $center must be centered in ${bitmap.height}px", kotlin.math.abs(center - bitmap.height / 2f) <= 2 * density)
        bitmap.recycle()
    }

    @Test fun bansUseFiveCompactSlotsPerTeamAndPreserveDuplicateChampions() {
        val context = RuntimeEnvironment.getApplication()
        com.example.data.WildRiftRepository.initChampions(context,forceReload=true)
        val slots = com.example.service.screen.emptyBanSlots().map {
            if(it.index==0) it.copy(championId="ahri",state=com.example.service.screen.BanState.CONFIRMED)
            else if(it.index==1) it.copy(state=com.example.service.screen.BanState.EMPTY) else it
        }
        compose.setContent { MyApplicationTheme { com.example.ui.components.DetectedBansCard(slots) } }
        for(side in listOf("ally","enemy")) for(index in 0..4) {
            compose.onNodeWithTag("draft_ban_${side}_$index",useUnmergedTree=true).assertExists()
        }
        compose.onNodeWithTag("draft_ban_ally_0",useUnmergedTree=true).assertContentDescriptionEquals("Ahri")
        compose.onNodeWithTag("draft_ban_enemy_0",useUnmergedTree=true).assertContentDescriptionEquals("Ahri")
    }

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
