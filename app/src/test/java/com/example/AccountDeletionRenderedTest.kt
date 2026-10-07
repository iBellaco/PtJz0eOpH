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
