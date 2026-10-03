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
@org.robolectric.annotation.SQLiteMode(org.robolectric.annotation.SQLiteMode.Mode.NATIVE)
class HubChampionNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `open and return from champions without an activity in Spanish`() = exerciseHub("es", "Volver")

    @Test fun `open and return from champions without an activity in Portuguese`() = exerciseHub("pt", "Voltar")

    @Test fun `deleting a creator build persists through reopening in the service hosted hub`() {
        val context = RuntimeEnvironment.getApplication()
        com.example.util.DynamicTranslations.loadSync(context)
        AppLanguage.select(context, "pt")
        WildRiftRepository.initChampions(context, forceReload = true)
        CustomChampionBuildsManager.init(context)
        // Deletion is reserved for an authorized author or administrator.
        val roleField = com.example.util.SubscriptionManager::class.java.getDeclaredField("_userRole").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (roleField.get(com.example.util.SubscriptionManager) as kotlinx.coroutines.flow.MutableStateFlow<String>).value = "admin"
        val first = com.example.data.local.CustomChampionBuildRecord(id = "delete-target", championId = "garen", championName = "Garen", buildTitle = "Build para excluir", role = "TOP", creatorName = "Teste")
        val second = first.copy(id = "keep-target", buildTitle = "Build preservada")
        val field = CustomChampionBuildsManager::class.java.getDeclaredField("_customBuilds").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val builds = field.get(CustomChampionBuildsManager) as kotlinx.coroutines.flow.MutableStateFlow<List<com.example.data.local.CustomChampionBuildRecord>>
        builds.value = listOf(first, second)
        val serviceView = FrameLayout(context)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalView provides serviceView) {
                assertNull(LocalOnBackPressedDispatcherOwner.current)
                MyApplicationTheme {
                    var open by remember { mutableStateOf(true) }
                    if (open) com.example.ui.components.AdminCreatorBuildsDialog { open = false }
                    else Button(onClick = { open = true }) { Text("Reabrir") }
                }
            }
        }
        compose.onNodeWithTag("delete_build_delete-target").performScrollTo().performClick()
        compose.waitUntil(timeoutMillis = 15000) { builds.value.none { it.id == "delete-target" } }
        org.junit.Assert.assertTrue(builds.value.any { it.id == "keep-target" })
        val prefs = context.getSharedPreferences("wr_custom_champion_builds_prefs", 0)
        org.junit.Assert.assertTrue(prefs.getStringSet("deleted_build_ids", emptySet()).orEmpty().contains("delete-target"))
        org.junit.Assert.assertTrue(prefs.getString("custom_champion_builds_json", "").orEmpty().length < 20000)
        val load = CustomChampionBuildsManager::class.java.getDeclaredMethod("loadFromLocalStorage", android.content.Context::class.java).apply { isAccessible = true }
        load.invoke(CustomChampionBuildsManager, context)
        org.junit.Assert.assertFalse(builds.value.any { it.id == "delete-target" })
        org.junit.Assert.assertTrue(builds.value.any { it.id == "keep-target" })
        compose.onNodeWithTag("creator_close_button").assertIsDisplayed().performClick()
        compose.onNodeWithText("Reabrir").performClick()
        compose.onNodeWithTag("delete_build_delete-target").assertDoesNotExist()
        compose.onNodeWithTag("creator_build_list").performScrollToNode(hasTestTag("delete_build_keep-target"))
        compose.onNodeWithTag("delete_build_keep-target").assertExists()
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(filePath = "build/reports/portuguese-rendered/creator-after-delete-pt.png")
    }

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
