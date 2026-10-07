package com.example

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.components.AccountDeletionCard
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
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
class AccountDeletionRenderedTest {
    @get:Rule val compose = createComposeRule()
    private fun language(value: String) {
        val context = RuntimeEnvironment.getApplication()
        DynamicTranslations.loadSync(context); AppLanguage.select(context,value)
    }
    private fun capture(name: String) {
        val folder=File("build/reports/portuguese-rendered").apply { mkdirs() }
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(folder,name+".png").path)
    }
    @Test fun `Spanish deletion needs both confirmations and password`()=confirmations("es")
    @Test fun `Portuguese deletion needs both confirmations and password`()=confirmations("pt")
    private fun confirmations(lang: String) {
        language(lang); var calls=0; var delivered:Long?=null
        compose.setContent { MyApplicationTheme { AccountDeletionCard("owner@test.invalid",submit={calls++;123456789L},onScheduled={delivered=it}) } }
        compose.onNodeWithTag("account_delete_open").performClick()
        capture("account-delete-first-$lang")
        compose.onNodeWithTag("account_delete_first_cancel").performClick()
        assertEquals(0,calls)
        compose.onNodeWithTag("account_delete_open").performClick()
        compose.onNodeWithTag("account_delete_first_confirm").performClick()
        compose.onNodeWithTag("account_delete_final_confirm").assertIsNotEnabled()
        capture("account-delete-final-$lang")
        compose.onNodeWithTag("account_delete_second_cancel").performClick()
        assertEquals(0,calls); assertNull(delivered)
        compose.onNodeWithTag("account_delete_open").performClick()
        compose.onNodeWithTag("account_delete_first_confirm").performClick()
        compose.onNodeWithTag("account_delete_password").performTextInput("test-password")
        compose.onNodeWithTag("account_delete_final_confirm").performClick()
        compose.waitForIdle()
        assertEquals(1,calls);assertEquals(123456789L,delivered ?: -1L)
        compose.onAllNodesWithTag("account_delete_final_confirm").assertCountEquals(0)
    }
    @Test fun `failure never claims a scheduled Spanish deletion`()=failure("es")
    @Test fun `failure never claims a scheduled Portuguese deletion`()=failure("pt")
    private fun failure(lang: String) {
        language(lang);var calls=0;var delivered=false
        compose.setContent { MyApplicationTheme { AccountDeletionCard("owner@test.invalid",submit={calls++;error("rejected")},onScheduled={delivered=true}) } }
        compose.onNodeWithTag("account_delete_open").performClick()
        compose.onNodeWithTag("account_delete_first_confirm").performClick()
        compose.onNodeWithTag("account_delete_password").performTextInput("wrong-password")
        compose.onNodeWithTag("account_delete_final_confirm").performClick();compose.waitForIdle()
        compose.onNodeWithTag("account_delete_error").assertIsDisplayed()
        compose.onNodeWithTag("account_delete_final_confirm").assertIsNotEnabled()
        assertEquals(1,calls);assertFalse(delivered)
    }
    @Test fun `Spanish legal policy states recovery data and contact`()=legal("es")
    @Test fun `Portuguese legal policy states recovery data and contact`()=legal("pt")
    private fun legal(lang: String) {
        language(lang)
        compose.setContent { MyApplicationTheme { PrivacyPolicyDialog(onDismiss={}) } }
        val title=if(lang=="pt") "6. Exclusão da conta e retenção" else "6. Eliminación de cuenta y conservación"
        compose.onNodeWithText(title).performScrollTo().assertIsDisplayed()
        capture("legal-recovery-$lang")
        compose.onNodeWithTag("legal_privacy_contact").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("DevWildRiftCoach@gmail.com",substring=true).assertExists()
        capture("legal-contact-$lang")
        compose.onAllNodesWithText("100% legal",substring=true).assertCountEquals(0)
        compose.onAllNodesWithText("confidencialidad absoluta",substring=true).assertCountEquals(0)
    }
}
