package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EconomyPendingStatus() {
    val user = AuthManager.getAuth()?.currentUser ?: return
    if (AuthManager.isGuestOrUnauthenticated(user)) return
    var pending by remember(user.uid) { mutableStateOf(false) }
    DisposableEffect(user.uid) {
        val listener = FirebaseFirestore.getInstance().collection("economy_requests").document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error == null) pending = snapshot?.getString("status") in setOf("PENDING", "PROCESSING")
            }
        onDispose { listener.remove() }
    }
    if (pending) Text(appTr("Solicitud en espera. El saldo se actualizará al confirmarse; no la repitas."),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("economy_pending_status"))
}
