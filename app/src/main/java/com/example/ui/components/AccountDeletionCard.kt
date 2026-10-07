package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DangerRed
import com.example.util.localizedString
import kotlinx.coroutines.launch

@Composable
fun AccountDeletionCard(
    email: String,
    submit: suspend (String) -> Long,
    onScheduled: (Long) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun dismiss() { if (!busy) { step = 0; password = ""; failed = false } }
    Column(Modifier.fillMaxWidth().testTag("account_deletion_card")) {
        Text(localizedString(R.string.account_delete_summary), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        CoachOutlinedButton(onClick = { step = 1 },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("account_delete_open")) {
            Text(localizedString(R.string.account_delete_title), color = DangerRed)
        }
    }
    if (step != 0) AlertDialog(
        onDismissRequest = ::dismiss,
        title = { Text(localizedString(if (step == 1) R.string.account_delete_first_title else R.string.account_delete_second_title)) },
        text = {
            val focus = LocalFocusManager.current
            if (step == 1) Text(localizedString(R.string.account_delete_first_body))
            else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(localizedString(R.string.account_delete_second_body, email))
                OutlinedTextField(value = password, onValueChange = { password = it; failed = false },
                    enabled = !busy, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                    label = { Text(localizedString(R.string.account_delete_password)) },
                    modifier = Modifier.fillMaxWidth().testTag("account_delete_password"))
                if (busy) CircularProgressIndicator(Modifier.size(24.dp).testTag("account_delete_busy"))
                if (failed) Text(localizedString(R.string.account_delete_error), color = DangerRed,
                    modifier = Modifier.testTag("account_delete_error"))
            }
        },
        confirmButton = {
            val focus = LocalFocusManager.current
            if (step == 1) CoachButton(onClick = { step = 2 },
                modifier = Modifier.heightIn(min = 48.dp).testTag("account_delete_first_confirm")) {
                Text(localizedString(R.string.continuar))
            }
            else CoachButton(enabled = !busy && password.isNotBlank(), onClick = {
            if (busy) return@CoachButton
            focus.clearFocus()
            busy = true; failed = false
            scope.launch {
                try {
                    val deadline = submit(password)
                    password = ""; step = 0; busy = false
                    onScheduled(deadline)
                } catch (failure: Exception) {
                    if (failure is kotlinx.coroutines.CancellationException) throw failure
                    password = ""; busy = false; failed = true
                }
            }
        }, modifier = Modifier.heightIn(min = 48.dp).testTag("account_delete_final_confirm")) {
            Text(localizedString(R.string.account_delete_submit))
        } },
        dismissButton = { CoachTextButton(enabled = !busy, onClick = ::dismiss,
            modifier = Modifier.heightIn(min = 48.dp).testTag(if (step == 1) "account_delete_first_cancel" else "account_delete_second_cancel")) {
            Text(localizedString(R.string.cancelar))
        } }
    )
}
