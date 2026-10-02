package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.StreamChannelUrl
import com.example.data.StreamerPublicationPolicy
import com.example.data.StreamerRepository
import com.example.util.localizedString
import com.example.util.AuthManager
import com.example.util.SubscriptionManager
import com.example.model.RolePanelAccess
import androidx.compose.ui.platform.testTag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.launch

private val StreamGold = Color(0xFFD4AF37)
private val StreamBackground = Color(0xFF111827)

@Composable
private fun streamerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White, disabledTextColor = Color.LightGray,
    focusedLabelColor = StreamGold, unfocusedLabelColor = Color.LightGray, disabledLabelColor = Color.Gray,
    cursorColor = StreamGold, focusedBorderColor = StreamGold, unfocusedBorderColor = Color.Gray
)

@Composable
private fun liveEntries(): Pair<List<Map<String, Any>>, Boolean> {
    var entries by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var available by remember { mutableStateOf(false) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    DisposableEffect(uid) {
        val listener = if (uid != null) StreamerRepository.registry.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            available = error == null && snapshot != null && !snapshot.metadata.isFromCache
            if (error == null && snapshot != null) entries = StreamerRepository.entries(snapshot.get("entries"))
        } else null
        onDispose { listener?.remove() }
    }
    return entries to available
}

@Composable
private fun operationError(result: Result<Unit>?): String? {
    if (result == null || result.isSuccess) return null
    val cause = generateSequence(result.exceptionOrNull()) { it.cause }.mapNotNull { it.message }.joinToString(" ")
    val id = when {
        cause.contains("streamer_expired") -> R.string.streamer_expired
        cause.contains("streamer_max") -> R.string.streamer_max
        cause.contains("streamer_name_error") -> R.string.streamer_name_error
        cause.contains("streamer_url_error") -> R.string.streamer_url_error
        cause.contains("streamer_role_error") -> R.string.streamer_role_error
        cause.contains("streamer_pending") -> R.string.streamer_pending
        else -> R.string.streamer_error
    }
    return localizedString(id)
}

@Composable
fun LiveStreamersRow() {
    val (entries, _) = liveEntries()
    val uri = LocalUriHandler.current
    val role by SubscriptionManager.userRole.collectAsState()
    val adminClaim by AuthManager.isAdminClaim.collectAsState()
    val isAdmin = RolePanelAccess.isAdministrator(role, adminClaim)
    if (entries.isNotEmpty()) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            entries.take(StreamerPublicationPolicy.MAX_LIVE).forEach { item ->
                val channel = StreamChannelUrl.parse(item["channelUrl"] as? String ?: "", allowAdminTest = isAdmin)
                if (channel != null) LiveStreamerChip(item["channelName"] as? String ?: "") { runCatching { uri.openUri(channel.url) } }
            }
        }
    }
}

@Composable
fun StreamerUrlRecommendations(isAdmin: Boolean, enabled: Boolean = true, onSelectUrl: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(localizedString(R.string.streamer_url_recommendation), color = Color.LightGray)
        TextButton(onClick = { onSelectUrl("https://www.twitch.tv/riotgames") }, enabled = enabled,
            modifier = Modifier.testTag("streamer_channel_example")) {
            Text("https://www.twitch.tv/riotgames")
        }
        if (isAdmin) {
            Text(localizedString(R.string.streamer_google_recommendation), color = Color.LightGray)
            TextButton(onClick = { onSelectUrl("https://www.google.com") }, enabled = enabled,
                modifier = Modifier.testTag("streamer_admin_google_example")) {
                Text("https://www.google.com")
            }
        }
    }
}

@Composable
fun StreamerPanelDialog(onDismiss: () -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val role by SubscriptionManager.userRole.collectAsState()
    val adminClaim by AuthManager.isAdminClaim.collectAsState()
    val isAdmin = RolePanelAccess.isAdministrator(role, adminClaim)
    val (entries, registryAvailable) = liveEntries()
    var request by remember(uid) { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var publications by remember(uid) { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var historyError by remember(uid) { mutableStateOf(false) }
    val now = streamerClock()
    var submitted by remember { mutableStateOf(false) }
    var requestAvailable by remember(uid) { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Result<Unit>?>(null) }
    val scope = rememberCoroutineScope()
    DisposableEffect(uid) {
        val listener = StreamerRepository.requests.document(uid).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            requestAvailable = error == null && snapshot != null && !snapshot.metadata.isFromCache
            if (error == null && snapshot != null) {
                request = snapshot.data.orEmpty()
                if (name.isBlank()) name = snapshot.getString("channelName").orEmpty()
                if (url.isBlank()) url = snapshot.getString("channelUrl").orEmpty()
            }
        }
        val historyListener = StreamerRepository.history(uid).addSnapshotListener { snapshot, error ->
            historyError = error != null
            if (error == null && snapshot != null) publications = snapshot.documents.mapNotNull { it.data }
        }
        onDispose { listener.remove(); historyListener.remove() }
    }
    val active = entries.any { it["userId"] == uid }
    val expired = StreamerPublicationPolicy.isExpired(request, now)
    val pending = request["status"] == "PENDING" && !expired
    LaunchedEffect(uid, request["publicationId"], expired) {
        if (expired) {
            val expiration = StreamerRepository.expire(uid)
            if (expiration.isFailure) result = expiration
        }
    }
    val maximum = entries.size >= StreamerPublicationPolicy.MAX_LIVE
    Dialog(onDismissRequest = { if (!busy) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(0.95f).heightIn(max = 650.dp), shape = RoundedCornerShape(16.dp), color = StreamBackground, border = BorderStroke(1.dp, StreamGold)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(localizedString(R.string.streamer_panel), color = StreamGold, style = MaterialTheme.typography.titleLarge)
                Text(localizedString(R.string.streamer_requirement), color = Color.White)
                Text(localizedString(R.string.streamer_expiry_notice), color = Color.LightGray)
                StreamerSubmissionFeedback(busy, submitted)
                OutlinedTextField(name, { name = it; result = null }, label = { Text(localizedString(R.string.streamer_name)) }, colors = streamerFieldColors(), singleLine = true, enabled = !busy && !pending && !active, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(url, { url = it; result = null }, label = { Text(localizedString(R.string.streamer_url)) }, colors = streamerFieldColors(), singleLine = true, enabled = !busy && !pending && !active, modifier = Modifier.fillMaxWidth())
                StreamerUrlRecommendations(isAdmin, enabled = !busy && !pending && !active) { url = it; result = null }
                Text(localizedString(R.string.streamer_count, entries.size), color = Color.White)
                if (maximum) Text(localizedString(R.string.streamer_max), color = StreamGold)
                when {
                    active -> Text(localizedString(R.string.streamer_approved), color = Color.White)
                    pending -> Text(localizedString(R.string.streamer_pending), color = Color.White)
                    request["status"] == "REJECTED" -> Text(localizedString(R.string.streamer_rejected), color = Color.White)
                }
                operationError(result)?.let { Text(it, color = Color(0xFFFF8A80)) }
                if (!registryAvailable || !requestAvailable) Text(localizedString(R.string.streamer_loading), color = Color.White)
                if (active) Button(onClick = { busy = true; scope.launch { submitted = false; result = StreamerRepository.end(uid); busy = false } }, enabled = !busy && registryAvailable) { Text(localizedString(R.string.streamer_end)) }
                else Button(onClick = { busy = true; scope.launch { result = StreamerRepository.submit(name, url); submitted = result?.isSuccess == true; busy = false } },
                    enabled = !busy && registryAvailable && requestAvailable && !maximum && !pending && name.trim().length in 2..60 && StreamChannelUrl.parse(url, allowAdminTest = isAdmin) != null) { Text(localizedString(R.string.streamer_submit)) }
                if (url.isNotBlank() && StreamChannelUrl.parse(url, allowAdminTest = isAdmin) == null) Text(localizedString(R.string.streamer_url_error), color = Color(0xFFFF8A80))
                val history = listOfNotNull(request.takeIf { it.isNotEmpty() }) + publications
                StreamerPublicationHistory(history, now)
                if (historyError) Text(localizedString(R.string.streamer_error), color = Color(0xFFFF8A80))
                TextButton(onClick = onDismiss, enabled = !busy) { Text(localizedString(R.string.streamer_close)) }
            }
        }
    }
}

/** Embedded in the existing verification and secondary-role review dialog. */
@Composable
fun StreamerReviewPanel(modifier: Modifier = Modifier) {
    val (entries, available) = liveEntries()
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var requestAvailable by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Result<Unit>?>(null) }
    val scope = rememberCoroutineScope()
    val uri = LocalUriHandler.current
    val now = streamerClock()
    val expiredRequests = requests.filter { StreamerPublicationPolicy.isExpired(it, now) }
    val pendingRequests = requests.filterNot { StreamerPublicationPolicy.isExpired(it, now) }
    LaunchedEffect(expiredRequests.map { it["id"] }) {
        expiredRequests.forEach { request ->
            val expiration = StreamerRepository.expire(request["id"] as String)
            if (expiration.isFailure) result = expiration
        }
    }
    DisposableEffect(Unit) {
        val listener = StreamerRepository.requests.whereEqualTo("status", "PENDING").addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            requestAvailable = error == null && snapshot != null && !snapshot.metadata.isFromCache
            if (error == null && snapshot != null) requests = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }.sortedBy { (it["submittedAtMillis"] as? Number)?.toLong() ?: 0L }
        }
        onDispose { listener.remove() }
    }
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(localizedString(R.string.streamer_requirement), color = Color.White)
        Text(localizedString(R.string.streamer_count, entries.size), color = StreamGold)
        if (entries.size >= 5) Text(localizedString(R.string.streamer_max), color = StreamGold)
        operationError(result)?.let { Text(it, color = Color(0xFFFF8A80)) }
        if (!available || !requestAvailable) Text(localizedString(R.string.streamer_loading), color = Color.White)
        entries.forEach { item ->
            Row(Modifier.fillMaxWidth()) {
                Text(item["channelName"] as? String ?: "", modifier = Modifier.weight(1f), color = Color.White)
                TextButton(onClick = { busy = true; scope.launch { result = StreamerRepository.end(item["userId"] as String); busy = false } }, enabled = !busy && available) { Text(localizedString(R.string.streamer_end)) }
            }
        }
        if (requestAvailable && pendingRequests.isEmpty()) Text(localizedString(R.string.streamer_empty), color = Color.White)
        pendingRequests.forEach { request ->
            key(request["id"]) {
                var verified by remember { mutableStateOf(false) }
                val channel = StreamChannelUrl.parse(request["channelUrl"] as? String ?: "", allowAdminTest = request["adminTest"] == true)
                val uid = request["id"] as String
                Surface(color = Color(0xFF1F2937), shape = RoundedCornerShape(10.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(request["channelName"] as? String ?: "", color = StreamGold, style = MaterialTheme.typography.titleMedium)
                        Text("${(request["userName"] as? String).orEmpty()} • ${(request["platform"] as? String).orEmpty()}", color = Color.White)
                        Text(request["channelUrl"] as? String ?: "", color = Color.White)
                        TextButton(onClick = { channel?.let { runCatching { uri.openUri(it.url) } } }, enabled = channel != null) { Text(localizedString(R.string.streamer_open)) }
                        Row { Checkbox(checked = verified, onCheckedChange = { verified = it }, enabled = !busy); Text(localizedString(R.string.streamer_verify), color = Color.White, modifier = Modifier.weight(1f)) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { busy = true; scope.launch { result = StreamerRepository.review(uid, true, verified); busy = false } }, enabled = !busy && available && requestAvailable && verified && channel != null && entries.size < 5) { Text(localizedString(R.string.streamer_approve)) }
                            TextButton(onClick = { busy = true; scope.launch { result = StreamerRepository.review(uid, false, false); busy = false } }, enabled = !busy && requestAvailable) { Text(localizedString(R.string.streamer_reject)) }
                        }
                    }
                }
            }
        }
    }
}
