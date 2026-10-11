package com.example

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.ui.auth.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AuthFormViewportTest {
    @get:Rule val compose = createComposeRule()

    @Test fun registrationIsReachableOnSmallScreensWithLargeText() = checkRegistration("es")
    @Test fun portugueseRegistrationRemainsReachableAfterViewportShrinks() = checkRegistration("pt")

    private fun checkRegistration(language: String) {
        val context = RuntimeEnvironment.getApplication()
        AppLanguage.select(context, language)
        val viewModel = AuthViewModel().apply {
            updatePassword("Coach-Strong-123!")
            updateConfirmPassword("Coach-Strong-123!")
        }
        val height = mutableStateOf(420.dp)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f)) {
                MyApplicationTheme {
                    Box(Modifier.size(320.dp, height.value)) {
                        AuthFormContainer(AuthScreenType.REGISTER) {
                            RegisterScreen(viewModel, onNavigateToLogin = {}, onRegisterSuccess = {})
                        }
                    }
                }
            }
        }
        try {
            val scroll = compose.onNodeWithTag("auth_form_scroll")
            val range = scroll.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange]
            assertTrue("Registration must scroll when its contents exceed the viewport", range.maxValue() > 0f)
            scroll.performTouchInput { swipeUp() }
            assertTrue("A real swipe must move the form", range.value() > 0f)
            compose.onNodeWithTag("auth_register_submit").performScrollTo()
                .assertIsDisplayed().assertIsEnabled().assertHasClickAction()

            // Reproduce the reduced space available when the keyboard opens or the device rotates.
            compose.runOnIdle { height.value = 180.dp }
            compose.onNodeWithTag("auth_register_submit").performScrollTo()
                .assertIsDisplayed().assertIsEnabled().assertHasClickAction()
            compose.onNodeWithText(com.example.util.trStr(language, "Inicia sesión"))
                .performScrollTo().assertIsDisplayed()
        } finally {
            AppLanguage.select(context, "es")
        }
    }
}
