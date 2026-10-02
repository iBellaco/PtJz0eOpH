package com.example

import android.app.Application
import android.widget.FrameLayout
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.WildRiftRepository
import com.example.data.local.CustomChampionBuildsManager
import com.example.data.local.FavoriteChampionsManager
import com.example.model.Champion
import com.example.ui.screens.ChampionDetailSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w330dp-h720dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HubChampionNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `open and return from champions without an activity in Spanish`() = exerciseHub("es", "Volver")

    @Test fun `open and return from champions without an activity in Portuguese`() = exerciseHub("pt", "Voltar")

    private fun exerciseHub(language: String, backLabel: String) {
        val context = RuntimeEnvironment.getApplication()
        AppLanguage.select(context, language)
        WildRiftRepository.initChampions(context, forceReload = true)
        FavoriteChampionsManager.init(context)
        CustomChampionBuildsManager.init(context)
        val champions = listOf("ahri", "garen", "hwei").map { id ->
            WildRiftRepository.champions.first { it.id == id }
        }
        // The real hub attaches a ComposeView to a service, with lifecycle owners
        // but no activity or OnBackPressedDispatcherOwner. An Activity-backed
        // Compose test would otherwise hide the crash reported in the hub.
        val serviceView = FrameLayout(context)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalView provides serviceView) {
                assertNull("Service-hosted hub must have no activity back dispatcher", LocalOnBackPressedDispatcherOwner.current)
                MyApplicationTheme {
                    var selected by remember { mutableStateOf<Champion?>(null) }
                    Box(Modifier.fillMaxSize()) {
                        if (selected == null) {
                            Column {
                                Text("Hub")
                                champions.forEach { champion ->
                                    Button(onClick = { selected = champion }) { Text(champion.name) }
                                }
                            }
                        } else {
                            ChampionDetailSheet(isOverlay = true, champion = selected, onDismiss = { selected = null })
                        }
                    }
                }
            }
        }
        for (champion in champions) {
            compose.onNodeWithText(champion.name).performClick()
            compose.onNodeWithText(backLabel).assertIsDisplayed()
            compose.onNodeWithText(champion.name).assertIsDisplayed()
            val output = File("build/reports/portuguese-rendered").apply { mkdirs() }
            compose.onAllNodes(isRoot()).onLast().captureRoboImage(
                filePath = File(output, "hub-${champion.id}-$language.png").path
            )
            compose.onNodeWithText(backLabel).performClick()
            compose.onNodeWithText("Hub").assertIsDisplayed()
        }
    }
}
