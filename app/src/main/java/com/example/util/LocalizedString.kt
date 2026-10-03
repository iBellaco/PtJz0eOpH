package com.example.util

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Resolve bundled UI resources using the explicit choice, including the first frame after selection. */
@Composable
fun localizedString(@StringRes id: Int, vararg arguments: Any): String {
    val context = LocalContext.current
    val language = currentAppLanguage()
    val resources = remember(context, language) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(AppLanguage.locale(language))
        context.createConfigurationContext(configuration).resources
    }
    return if (arguments.isEmpty()) resources.getString(id) else resources.getString(id, *arguments)
}
