package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ManagedUsersDirectory
import com.example.util.localizedString

@Composable
internal fun DirectorySyncLabel(state: ManagedUsersDirectory.State) {
    val message = when (state.status) {
        ManagedUsersDirectory.Status.SYNCING -> localizedString(R.string.directory_syncing)
        ManagedUsersDirectory.Status.OFFLINE -> localizedString(R.string.directory_offline)
        ManagedUsersDirectory.Status.ERROR -> localizedString(R.string.directory_error)
        ManagedUsersDirectory.Status.LIVE -> localizedString(R.string.directory_synced,
            java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.MEDIUM)
                .format(java.util.Date(state.lastSyncAt)))
    }
    Text(message, modifier = Modifier.testTag("directory_sync_status"), fontSize = 12.sp)
    if (state.lastSyncAt > 0 && state.status != ManagedUsersDirectory.Status.LIVE) {
        Text(localizedString(R.string.directory_synced,
            java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.MEDIUM)
                .format(java.util.Date(state.lastSyncAt))), fontSize = 12.sp)
    }
}
