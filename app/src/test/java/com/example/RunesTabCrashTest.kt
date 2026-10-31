package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.ui.screens.MetaAndDraftScreen
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.AppAssetImage

@RunWith(AndroidJUnit4::class)
class RunesTabCrashTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppAssetImageWithDrawable() {
        composeTestRule.setContent {
            AppAssetImage(
                url = "https://i.postimg.cc/XqmjT2tX/IMG-20260822-184232.jpg",
                contentDescription = "Electrocutar",
                fallbackText = "Electrocutar"
            )
        }
    }
}
