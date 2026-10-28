package com.example

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.service.OverlayVersusDraftBoard
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DraftOrderUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun unknownOrderNeverRendersTheRivalFirstBadgeAndUpdatesAfterEvidence() {
        val context = RuntimeEnvironment.getApplication()
        DynamicTranslations.loadSync(context)
        AppLanguage.select(context, "es-419")
        val known = mutableStateOf(false)
        val ally = mutableStateOf(false)
        compose.setContent {
            MyApplicationTheme {
                OverlayVersusDraftBoard(allySlots = emptyList(), enemySlots = emptyList(), activeUserRole = null,
                    isFirstPick = ally.value, isFirstPickKnown = known.value,
                    onToggleFirstPick = { known.value = true; ally.value = !ally.value },
                    onPickChampionForRole = { _, _ -> }, onRemoveChampionForRole = { _, _ -> })
            }
        }
        compose.onNodeWithText("1ª selección: por confirmar").assertExists()
        compose.onNodeWithText("1ª Selección: Rival").assertDoesNotExist()
        compose.onNodeWithText("1ª SELECCIÓN").assertDoesNotExist()
        compose.runOnIdle { known.value = true; ally.value = true }
        compose.onNodeWithText("1ª Selección: Aliados").assertExists()
        compose.onNodeWithTag("draft_first_pick_toggle").performClick()
        compose.onNodeWithText("1ª Selección: Rival").assertExists()
        compose.runOnIdle { known.value = false; AppLanguage.select(context, "pt-BR") }
        compose.onNodeWithText("1ª escolha: a confirmar").assertExists()
        compose.runOnIdle { AppLanguage.select(context, "es-419") }
    }
}
