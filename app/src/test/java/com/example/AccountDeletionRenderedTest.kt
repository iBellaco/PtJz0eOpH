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
    @Test fun `privacy email launches without requiring package visibility`() {
        language("es")
        var launched: android.content.Intent? = null
        val context = object : android.content.ContextWrapper(RuntimeEnvironment.getApplication()) {
            override fun startActivity(intent: android.content.Intent) { launched = intent }
        }
        com.example.util.PrivacyContact.open(context)
        assertEquals(android.content.Intent.ACTION_SENDTO, launched?.action)
        assertEquals("mailto", launched?.data?.scheme)
        assertTrue(launched!!.data.toString().contains(com.example.util.PrivacyContact.EMAIL))
        assertTrue(android.net.Uri.decode(launched!!.data.toString()).contains("consulta de privacidad"))
    }

    @Test fun `unavailable and blocked mail apps copy the privacy address and explain the fallback`() {
        for (lang in listOf("es", "pt")) {
            language(lang)
            for (blocked in listOf(false, true)) {
                val application = RuntimeEnvironment.getApplication()
                val context = object : android.content.ContextWrapper(application) {
                    override fun startActivity(intent: android.content.Intent) {
                        if (blocked) throw SecurityException("Blocked")
                        throw android.content.ActivityNotFoundException("No mail app")
                    }
                }
                com.example.util.PrivacyContact.open(context)
                val clipboard = application.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager
                assertEquals(com.example.util.PrivacyContact.EMAIL, clipboard.primaryClip!!.getItemAt(0).text)
                assertEquals(context.getString(com.example.R.string.legal_contact_copied),
                    org.robolectric.shadows.ShadowToast.getTextOfLatestToast())
            }
        }
    }
    private fun language(value: String) {
        val context = RuntimeEnvironment.getApplication()
        DynamicTranslations.loadSync(context); AppLanguage.select(context,value)
    }
    private fun capture(name: String) {
        val folder=File("build/reports/portuguese-rendered").apply { mkdirs() }
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(File(folder,name+".png").path)
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
        compose.onNodeWithTag("legal_public_policy").assertDoesNotExist()
        val context = RuntimeEnvironment.getApplication()
        compose.onNodeWithTag("legal_privacy_contact").performClick()
        val intent = org.robolectric.Shadows.shadowOf(context).nextStartedActivity
        assertEquals(android.content.Intent.ACTION_SENDTO, intent.action)
        compose.onNodeWithText("DevWildRiftCoach@gmail.com",substring=true).assertExists()
        capture("legal-contact-$lang")
        compose.onAllNodesWithText("100% legal",substring=true).assertCountEquals(0)
        compose.onAllNodesWithText("confidencialidad absoluta",substring=true).assertCountEquals(0)
    }
}
