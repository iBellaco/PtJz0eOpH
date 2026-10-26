package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.util.SystemPermissionHelper

/** Conditional advice: canDrawOverlays alone cannot identify restricted settings. */
@Composable
fun OverlayPermissionHelp() {
    val context = LocalContext.current
    Column {
        Text(stringResource(R.string.overlay_restricted_help),fontSize=12.sp,lineHeight=17.sp)
        CoachOutlinedButton(
            onClick={ SystemPermissionHelper.openApplicationDetails(context) },
            modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("overlay_app_settings")
        ) { Text(stringResource(R.string.overlay_app_settings)) }
    }
}
