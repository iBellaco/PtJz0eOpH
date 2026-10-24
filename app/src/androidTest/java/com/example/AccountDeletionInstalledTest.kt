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
        compose.waitForIdle()
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
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
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
        // Reopening a native dialog recreates its text semantics after focus.
        // Focus explicitly and settle that frame before resolving the input node.
        click("account_delete_password")
        compose.onNodeWithTag("account_delete_password").assertIsFocused()
        compose.onNodeWithTag("account_delete_password").performTextInput(value); frame()
        // Password semantics expose the transformed display value, not the secret.
        // Verify masking here and the exact original value at the submit boundary.
        compose.onNodeWithTag("account_delete_password").assertTextContains("\u2022".repeat(value.length))
        compose.onNodeWithTag("account_delete_password").performImeAction(); frame()
        compose.onNodeWithTag("account_delete_password").assertIsNotFocused()
    }
    @Test fun spanishDeletionNeedsBothConfirmationsAndPassword()=confirmations("es")
    @Test fun portugueseDeletionNeedsBothConfirmationsAndPassword()=confirmations("pt")
    @Test fun pendingRequestPreventsRepeatedSubmissionAndCancellation() {
        language("es");var calls=0;var delivered=false
        val reply=kotlinx.coroutines.CompletableDeferred<Long>()
        var submittedPassword:String?=null
        content { MyApplicationTheme { AccountDeletionCard("owner@test.invalid",submit={submittedPassword=it;calls++;reply.await()},onScheduled={delivered=true}) } }
        click("account_delete_open")
        openFinal()
        password("test-password")
        click("account_delete_final_confirm")
        compose.onNodeWithTag("account_delete_final_confirm").assertIsNotEnabled()
        compose.onNodeWithTag("account_delete_second_cancel").assertIsNotEnabled()
        compose.onNodeWithTag("account_delete_password").assertIsNotEnabled()
        assertEquals(1,calls);assertEquals("test-password",submittedPassword);assertFalse(delivered)
        compose.runOnIdle { reply.complete(123456789L) };frame()
        assertEquals(1,calls);assertTrue(delivered)
    }
    private fun confirmations(lang: String) {
        language(lang); var calls=0; var delivered:Long?=null;var submittedPassword:String?=null
        content { MyApplicationTheme { AccountDeletionCard("owner@test.invalid",submit={submittedPassword=it;calls++;123456789L},onScheduled={delivered=it}) } }
        click("account_delete_open")
        capture("account-delete-first-$lang")
        click("account_delete_first_cancel")
        assertEquals(0,calls)
        click("account_delete_open")
        openFinal()
        compose.onNodeWithTag("account_delete_final_confirm").assertIsNotEnabled()
        capture("account-delete-final-$lang")
        click("account_delete_second_cancel")
        assertEquals(0,calls); assertNull(delivered);assertNull(submittedPassword)
        click("account_delete_open")
        openFinal()
        password("test-password")
        click("account_delete_final_confirm")
        compose.waitForIdle()
        assertEquals(1,calls);assertEquals("test-password",submittedPassword);assertEquals(123456789L,delivered ?: -1L)
        compose.onAllNodesWithTag("account_delete_final_confirm").assertCountEquals(0)
    }
    @Test fun failureNeverClaimsScheduledSpanishDeletion()=failure("es")
    @Test fun failureNeverClaimsScheduledPortugueseDeletion()=failure("pt")
    private fun failure(lang: String) {
        language(lang);var calls=0;var delivered=false;var submittedPassword:String?=null
        content { MyApplicationTheme { AccountDeletionCard("owner@test.invalid",submit={submittedPassword=it;calls++;error("rejected")},onScheduled={delivered=true}) } }
        click("account_delete_open")
        openFinal()
        password("wrong-password")
        click("account_delete_final_confirm");compose.waitForIdle()
        compose.onNodeWithTag("account_delete_error").assertIsDisplayed()
        compose.onNodeWithTag("account_delete_final_confirm").assertIsNotEnabled()
        assertEquals(1,calls);assertEquals("wrong-password",submittedPassword);assertFalse(delivered)
    }
}
