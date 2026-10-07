package com.example

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ui.components.AccountDeletionCard
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Checks both deletion confirmations on the installed, signed R8 release. */
@RunWith(AndroidJUnit4::class)
class AccountDeletionInstalledTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun content(body: @Composable () -> Unit) {
        compose.activityRule.scenario.onActivity { it.setContent(content = body) }
    }
    private fun language(value: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        DynamicTranslations.loadSync(context); AppLanguage.select(context,value)
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val folder = File(instrumentation.targetContext.getExternalFilesDir(null), "coach-account-deletion-audit").apply { mkdirs() }
        val picture = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        try { File(folder, "$name.png").outputStream().use { picture.compress(Bitmap.CompressFormat.PNG,100,it) } }
        finally { picture.recycle() }
    }
    private fun frame() { compose.mainClock.advanceTimeBy(300); compose.waitForIdle() }
    private fun click(tag: String) {
        compose.onNodeWithTag(tag).performClick()
        if (!compose.mainClock.autoAdvance) frame()
    }
    private fun openFinal() {
        compose.onNodeWithTag("account_delete_first_confirm").performClick()
        // Freeze the automatically focused password cursor and progress animations;
        // every interaction still advances a frame and retains its behavioral assertions.
        compose.mainClock.autoAdvance = false
        frame()
    }
    private fun password(value: String) {
        compose.onNodeWithTag("account_delete_password").performTextInput(value); frame()
    }
    @Test fun `Spanish deletion needs both confirmations and password`()=confirmations("es")
    @Test fun `Portuguese deletion needs both confirmations and password`()=confirmations("pt")
    @Test fun `pending request prevents repeated submission and cancellation`() {
        language("es");var calls=0;var delivered=false
        val reply=kotlinx.coroutines.CompletableDeferred<Long>()
        content { MyApplicationTheme { AccountDeletionCard("owner@test.invalid",submit={calls++;reply.await()},onScheduled={delivered=true}) } }
        click("account_delete_open")
        openFinal()
        password("test-password")
        click("account_delete_final_confirm")
        compose.onNodeWithTag("account_delete_final_confirm").assertIsNotEnabled()
        compose.onNodeWithTag("account_delete_second_cancel").assertIsNotEnabled()
        compose.onNodeWithTag("account_delete_password").assertIsNotEnabled()
        assertEquals(1,calls);assertFalse(delivered)
        compose.runOnIdle { reply.complete(123456789L) };frame()
        assertEquals(1,calls);assertTrue(delivered)
    }
    private fun confirmations(lang: String) {
        language(lang); var calls=0; var delivered:Long?=null
        content { MyApplicationTheme { AccountDeletionCard("owner@test.invalid",submit={calls++;123456789L},onScheduled={delivered=it}) } }
        click("account_delete_open")
        capture("account-delete-first-$lang")
        click("account_delete_first_cancel")
        assertEquals(0,calls)
        click("account_delete_open")
        openFinal()
        compose.onNodeWithTag("account_delete_final_confirm").assertIsNotEnabled()
        capture("account-delete-final-$lang")
        click("account_delete_second_cancel")
        assertEquals(0,calls); assertNull(delivered)
        click("account_delete_open")
        openFinal()
        password("test-password")
        click("account_delete_final_confirm")
        compose.waitForIdle()
        assertEquals(1,calls);assertEquals(123456789L,delivered ?: -1L)
        compose.onAllNodesWithTag("account_delete_final_confirm").assertCountEquals(0)
    }
    @Test fun `failure never claims a scheduled Spanish deletion`()=failure("es")
    @Test fun `failure never claims a scheduled Portuguese deletion`()=failure("pt")
    private fun failure(lang: String) {
        language(lang);var calls=0;var delivered=false
        content { MyApplicationTheme { AccountDeletionCard("owner@test.invalid",submit={calls++;error("rejected")},onScheduled={delivered=true}) } }
        click("account_delete_open")
        openFinal()
        password("wrong-password")
        click("account_delete_final_confirm");compose.waitForIdle()
        compose.onNodeWithTag("account_delete_error").assertIsDisplayed()
        compose.onNodeWithTag("account_delete_final_confirm").assertIsNotEnabled()
        assertEquals(1,calls);assertFalse(delivered)
    }
}
