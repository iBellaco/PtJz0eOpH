package com.example

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.AvatarCatalog
import com.example.ui.components.AvatarSelectionBottomSheet
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w360dp-h480dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AvatarCatalogScrollTest {
    @get:Rule val compose = createComposeRule()
    @Test fun shortScreenCanScrollPastHeaderToLastAvatarAndReturn() {
        compose.mainClock.autoAdvance = false
        compose.setContent { MyApplicationTheme { AvatarSelectionBottomSheet({}, {}) } }
        compose.mainClock.advanceTimeBy(1000)
        val last = AvatarCatalog.avatars.filter { it.region == "Zaun" }.last()
        compose.onNodeWithTag("avatar_catalog_scroll").performScrollToNode(hasTestTag("avatar_card_${last.id}"))
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithTag("avatar_card_${last.id}").assertIsDisplayed()
        compose.onNodeWithTag("avatar_catalog_scroll").performScrollToIndex(0)
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithText("Avatares", substring=false).assertExists()
    }
}
