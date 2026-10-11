package com.example

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.toPixelMap
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

    @Test fun everyStyleShowsParticlesAboveItsBackgroundAndGivesUserTheSameTint() {
        var style by mutableStateOf(CoachNavStyle.ORIGINAL)
        var selected by mutableIntStateOf(4)
        var particles by mutableStateOf(false)
        AppThemeManager.setNavBarOption(com.example.ui.theme.NavBarColorOption.THEME_AUTO)
        compose.mainClock.autoAdvance = false
        compose.setContent { MyApplicationTheme {
            com.example.ui.components.CoachNavStyleRenderer(style,
                com.example.ui.components.CoachNavPreviewItems(), selected, {},
                androidx.compose.ui.Modifier.testTag("nav_pixels"), particlesEnabled = particles)
        } }
        for (option in CoachNavStyle.entries) {
            compose.runOnIdle { style = option; selected = 4; particles = false }
            compose.mainClock.advanceTimeBy(1000)
            fun assertTint(expected: androidx.compose.ui.graphics.Color, tolerance: Float = .03f) {
                val pixels = compose.onNodeWithTag("user_navigation_icon", useUnmergedTree = true).captureToImage().toPixelMap()
                var matches = 0
                for (x in 0 until pixels.width) for (y in 0 until pixels.height) {
                    val color = pixels[x, y]
                    if (kotlin.math.abs(color.red - expected.red) < tolerance &&
                        kotlin.math.abs(color.green - expected.green) < tolerance &&
                        kotlin.math.abs(color.blue - expected.blue) < tolerance) matches++
                }
                assertTrue("User tint must match $expected in $option", matches > 10)
            }
            assertTint(AppThemeManager.getNavBarSelectedIconColor())
            val off = compose.onNodeWithTag("nav_pixels").captureToImage().toPixelMap()
            compose.onNodeWithTag("nav_particles").assertDoesNotExist()
            compose.runOnIdle { particles = true }
            compose.mainClock.advanceTimeBy(1000)
            compose.onNodeWithTag("nav_particles").assertIsDisplayed()
            val on = compose.onNodeWithTag("nav_pixels").captureToImage().toPixelMap()
            var changed = 0
            for (x in 0 until off.width step 3) for (y in 0 until off.height step 3)
                if (off[x, y] != on[x, y]) changed++
            assertTrue("Particles must change visible pixels in $option", changed > 25)
            compose.runOnIdle { particles = false; selected = 0 }
            compose.mainClock.advanceTimeBy(1000)
            assertTint(AppThemeManager.getNavBarUnselectedColor(), .16f)
        }
    }

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
        var density = 1f
        compose.setContent { MyApplicationTheme {
            density = androidx.compose.ui.platform.LocalDensity.current.density
            com.example.ui.components.DetectedBansCard(slots)
        } }
        assertTrue("Bans must fit a compact row", compose.onNodeWithTag("draft_detected_bans")
            .fetchSemanticsNode().boundsInRoot.height <= 48 * density)
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
        // The catalogue opens a second window over an animated bottom sheet. Advance
        // its clock explicitly instead of waiting for window animations to become idle.
        compose.mainClock.autoAdvance = false
        compose.setContent { MyApplicationTheme { ThemeCustomizationBottomSheet(isPremium=true, onDismiss={}) } }
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithTag("theme_nav_catalog").performScrollTo().performClick()
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithTag("theme_nav_style_liquid").performClick()
        compose.mainClock.advanceTimeBy(1000)
        assertEquals(CoachNavStyle.ORIGINAL, AppThemeManager.currentNavStyle)
        compose.onNodeWithTag("theme_nav_preview").performScrollTo().assertExists()
        compose.onNodeWithTag("coach_nav_item_3").performClick()
        compose.mainClock.advanceTimeBy(1000)
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
        compose.onNodeWithTag("theme_nav_catalog").performScrollTo().performClick()
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
    @Test fun everyNavigationStyleShowsAndClearsTheSameAnimatedNotification() {
        var style by mutableStateOf(CoachNavStyle.ORIGINAL)
        var count by mutableIntStateOf(2)
        compose.setContent { MyApplicationTheme {
            val items = com.example.ui.components.CoachNavPreviewItems().mapIndexed { index, item ->
                if (index == 4) item.copy(notificationCount = count) else item
            }
            com.example.ui.components.CoachNavStyleRenderer(style, items, 0, {})
        } }
        for (option in CoachNavStyle.entries) {
            compose.runOnIdle { style = option; count = 2 }
            compose.onNodeWithTag("user_navigation_badge", useUnmergedTree = true).assertIsDisplayed()
            compose.onNodeWithTag("user_navigation_icon", useUnmergedTree = true).assertContentDescriptionEquals(com.example.util.appTr("Notificaciones"))
            compose.runOnIdle { count = 0 }
            compose.onNodeWithTag("user_navigation_badge", useUnmergedTree = true).assertDoesNotExist()
        }
    }

    @Test fun logoutCompletesOnceAfterTheVisibleAnimationAndIgnoresExtraTaps() {
        var logouts = 0
        compose.mainClock.autoAdvance = false
        compose.setContent { MyApplicationTheme { AnimatedLogoutButton(onLogoutComplete = { logouts++ }) } }
        compose.onNodeWithTag("animated_logout_button").performClick()
        compose.mainClock.advanceTimeBy(1000)
        assertEquals(0, logouts)
        compose.onNodeWithTag("animated_logout_button").performClick()
        compose.mainClock.advanceTimeBy(2100)
        assertEquals(0, logouts)
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        assertEquals(1, logouts)
    }

    @Test fun contentAvoidsLeftAndTopCutoutsWithoutGuessingThePhoneModel() {
        var insets by mutableStateOf(androidx.compose.foundation.layout.WindowInsets(left = 60, top = 0))
        compose.setContent {
            com.example.ui.components.SafeDisplayContent(cutout = insets) {
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .testTag("cutout_content"))
            }
        }
        assertEquals(60f, compose.onNodeWithTag("cutout_content").fetchSemanticsNode().boundsInRoot.left, 1f)
        compose.runOnIdle { insets = androidx.compose.foundation.layout.WindowInsets(top = 40) }
        assertEquals(40f, compose.onNodeWithTag("cutout_content").fetchSemanticsNode().boundsInRoot.top, 1f)
    }

}
