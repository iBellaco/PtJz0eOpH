package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachFilterChip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    DisposableEffect(Unit) {
        var listener: com.google.firebase.firestore.ListenerRegistration? = null
        fun attach() {
            listener?.remove()
            listener = StreamerRepository.registry.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                available = error == null && snapshot != null && !snapshot.metadata.isFromCache
                if (error == null && snapshot != null) entries = StreamerRepository.entries(snapshot.get("entries"))
            }
        }
        // Subscribe once initially; retry only when the guest/session identity actually changes.
        val auth = FirebaseAuth.getInstance()
        var listenerUid = auth.currentUser?.uid
        attach()
        val authListener = FirebaseAuth.AuthStateListener { updatedAuth ->
            val uid = updatedAuth.currentUser?.uid
            if (uid != listenerUid) { listenerUid = uid; attach() }
        }
        auth.addAuthStateListener(authListener)
        onDispose { auth.removeAuthStateListener(authListener); listener?.remove() }
    }
    return entries to available
}

@Composable
private fun operationError(result: Result<Unit>?): String? {
    if (result == null || result.isSuccess) return null
    val cause = generateSequence(result.exceptionOrNull()) { it.cause }.mapNotNull { it.message }.joinToString(" ")
    val cloudCode = generateSequence(result.exceptionOrNull()) { it.cause }
        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>().firstOrNull()?.code
    val id = when {
        cloudCode == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> R.string.streamer_permission_error
        cloudCode == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAUTHENTICATED -> R.string.streamer_session_error
        cause.contains("streamer_metrics_error") -> R.string.streamer_metrics_error
        cause.contains("streamer_history_error") -> R.string.streamer_history_error
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
    val context = androidx.compose.ui.platform.LocalContext.current
    LiveStreamersContent(entries) { item, url ->
        if (runCatching { uri.openUri(url) }.isSuccess) com.example.data.StreamerClickWorker.enqueue(context, item)
    }
}

@Composable
fun LiveStreamersContent(entries: List<Map<String, Any>>,
    onOpen: (Map<String, Any>, String) -> Unit) {
    if (entries.isNotEmpty()) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            entries.take(StreamerPublicationPolicy.MAX_LIVE).forEach { item ->
                val channel = StreamChannelUrl.approved(item["channelUrl"] as? String ?: "")
                if (channel != null) LiveStreamerChip(item["channelName"] as? String ?: "") { onOpen(item, channel.url) }
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val role by SubscriptionManager.userRole.collectAsState()
    val adminClaim by AuthManager.isAdminClaim.collectAsState()
    val isAdmin = RolePanelAccess.isAdministrator(role, adminClaim)
    val (entries, registryAvailable) = liveEntries()
    var request by remember(uid) { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var publications by remember(uid) { mutableStateOf(com.example.data.StreamerHistoryCache.records(context, uid)) }
    var historyError by remember(uid) { mutableStateOf(false) }
    var clickMetrics by remember(uid) { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var clicksAvailable by remember(uid) { mutableStateOf(false) }
    val now = streamerClock()
    var submitted by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var requestAvailable by remember(uid) { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(3) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Result<Unit>?>(null) }
    val scope = rememberCoroutineScope()
    DisposableEffect(uid) {
        val listener = StreamerRepository.requests.document(uid).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            requestAvailable = error == null && snapshot != null && !snapshot.metadata.isFromCache
            if (error == null && snapshot != null) {
                request = snapshot.data.orEmpty()
                publications = com.example.data.StreamerHistoryCache.merge(context, uid, listOfNotNull(request.takeIf { it.isNotEmpty() }))
            }
        }
        val historyListener = StreamerRepository.history(uid).addSnapshotListener { snapshot, error ->
            historyError = error != null
            if (error == null && snapshot != null) publications = com.example.data.StreamerHistoryCache.merge(context, uid, snapshot.documents.mapNotNull { it.data })
        }
        val metricsListener = StreamerRepository.metrics.whereEqualTo("userId", uid).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            clicksAvailable = error == null && snapshot != null && !snapshot.metadata.isFromCache
            if (error == null && snapshot != null) {
                clickMetrics = snapshot.documents.mapNotNull { doc -> doc.getLong("clickCount")?.let { doc.id to it } }.toMap()
            }
        }
        onDispose { listener.remove(); historyListener.remove(); metricsListener.remove() }
    }
    val activeEntry = entries.firstOrNull { it["userId"] == uid }
    val active = activeEntry != null
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
                PanelReadControl(com.example.data.NotificationPanel.STREAMER)
                Text(localizedString(R.string.streamer_requirement), color = Color.White)
                Text(localizedString(R.string.streamer_expiry_notice), color = Color.LightGray)

                // Aviso de Regla de Categoría
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("streamer_category_rule_warning")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = com.example.util.tr("Regla importante: Si eliges la categoría incorrecta o contenido que no corresponda a Wild Rift, tu cuenta será suspendida."),
                            color = Color(0xFFEF4444),
                            fontSize = 11.5.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }

                StreamerSubmissionFeedback(busy && submitting, submitted)
                OutlinedTextField(name, { name = it; result = null }, label = { Text(localizedString(R.string.streamer_name)) }, colors = streamerFieldColors(), singleLine = true, enabled = !busy && !pending && !active, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(url, { url = it; result = null }, label = { Text(localizedString(R.string.streamer_url)) }, colors = streamerFieldColors(), singleLine = true, enabled = !busy && !pending && !active, modifier = Modifier.fillMaxWidth())
                StreamerUrlRecommendations(isAdmin, enabled = !busy && !pending && !active) { url = it; result = null }

                // Selector de duración de publicación
                if (!active && !pending) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = com.example.util.tr("Tiempo de visualización en vivo:"),
                            color = StreamGold,
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        ) {
                            listOf(
                                3 to "3 Horas",
                                6 to "6 Horas",
                                12 to "12 Horas",
                                0 to "Extensible"
                            ).forEach { (hours, label) ->
                                CoachFilterChip(
                                    selected = selectedDuration == hours,
                                    onClick = { if (!busy && !pending && !active) selectedDuration = hours },
                                    label = { Text(com.example.util.tr(label), fontSize = 11.sp) },
                                    enabled = !busy && !pending && !active,
                                    modifier = Modifier.testTag("streamer_duration_${hours}h")
                                )
                            }
                        }
                    }
                }

                Text(localizedString(R.string.streamer_count, entries.size), color = Color.White)
                if (maximum) Text(localizedString(R.string.streamer_max), color = StreamGold)
                when {
                    active -> {
                        Text(localizedString(R.string.streamer_approved), color = Color.White)
                        val activeData = activeEntry.orEmpty()
                        val activeHours = StreamerPublicationPolicy.durationHours(activeData)
                        val activeDurationValue = if (activeHours <= 0) localizedString(R.string.streamer_duration_extensible) else localizedString(R.string.streamer_duration_hours, activeHours)
                        Text(localizedString(R.string.streamer_duration_selected, activeDurationValue), color = Color.White, modifier = Modifier.testTag("streamer_active_duration"))
                        val liveDeadline = StreamerPublicationPolicy.liveExpiresAt(activeData)
                        if (liveDeadline > 0L) {
                            val remainingSeconds = ((liveDeadline - now).coerceAtLeast(0L) + 999L) / 1000L
                            Text(localizedString(R.string.streamer_live_ends_in, remainingSeconds / 3600L, (remainingSeconds / 60L) % 60L, remainingSeconds % 60L), color = StreamGold, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.testTag("streamer_live_countdown"))
                        }
                        val activePubId = StreamerPublicationPolicy.publicationId(activeData)
                        val activeClicks = clickMetrics[activePubId] ?: (request["clickCount"] as? Number)?.toLong() ?: 0L
                        Text(localizedString(R.string.streamer_history_clicks, activeClicks), color = StreamGold, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                    pending -> Text(localizedString(R.string.streamer_pending), color = Color.White)
                    request["status"] == "REJECTED" -> Text(localizedString(R.string.streamer_rejected), color = Color.White)
                }
                operationError(result)?.let { Text(it, color = Color(0xFFFF8A80)) }
                if (!registryAvailable || !requestAvailable) Text(localizedString(R.string.streamer_loading), color = Color.White)
                if (active) Button(onClick = { submitting = false; busy = true; scope.launch { submitted = false; result = StreamerRepository.end(uid); busy = false } }, enabled = !busy && registryAvailable) { Text(localizedString(R.string.streamer_end)) }
                else Button(onClick = { submitting = true; busy = true; scope.launch { result = StreamerRepository.submit(name, url, selectedDuration); submitted = result?.isSuccess == true; if (submitted) { name = ""; url = "" }; busy = false } },
                    enabled = !busy && registryAvailable && requestAvailable && !maximum && !pending && name.trim().length in 2..60 && StreamChannelUrl.parse(url, allowAdminTest = isAdmin) != null) { Text(localizedString(R.string.streamer_submit)) }
                if (url.isNotBlank() && StreamChannelUrl.parse(url, allowAdminTest = isAdmin) == null) Text(localizedString(R.string.streamer_url_error), color = Color(0xFFFF8A80))
                val history = (publications + listOfNotNull(request.takeIf { it.isNotEmpty() })).distinctBy { StreamerPublicationPolicy.publicationId(it) }.map { item ->
                    val id = StreamerPublicationPolicy.publicationId(item)
                    val count = clickMetrics[id]
                    if (count != null) item + mapOf("clickCount" to count, "clicksLive" to clicksAvailable)
                    else if (StreamerPublicationPolicy.historyStatus(item, now) in listOf("PENDING", "REJECTED")) item + ("clickCount" to 0L)
                    else item
                }
                StreamerPublicationHistory(history, now)
                if (!clicksAvailable && history.any { it["status"] == "APPROVED" || it["status"] == "ENDED" }) Text(com.example.util.tr("No se pudieron consultar los clics. Vuelve a intentarlo."), color = Color(0xFFFF8A80))
                if (historyError) Text(localizedString(R.string.streamer_history_error), color = Color(0xFFFF8A80))
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
    // Counter repair is separate from approval; it must not overwrite its confirmed result.
    var metricRepairFailed by remember { mutableStateOf(false) }
    LaunchedEffect(entries) { if (entries.isNotEmpty()) metricRepairFailed = StreamerRepository.repairMetrics().isFailure }
    val expiredRequests = requests.filter { StreamerPublicationPolicy.isExpired(it, now) }
    val pendingRequests = requests.filterNot { StreamerPublicationPolicy.isExpired(it, now) }
    LaunchedEffect(expiredRequests.map { it["id"] }) {
        expiredRequests.forEach { request ->
            val expiration = StreamerRepository.expire(request["id"] as String)
            if (expiration.isFailure) result = expiration
        }
    }
    var clickMetrics by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    DisposableEffect(Unit) {
        val listener = StreamerRepository.requests.whereEqualTo("status", "PENDING").addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            requestAvailable = error == null && snapshot != null && !snapshot.metadata.isFromCache
            if (error == null && snapshot != null) requests = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }.sortedBy { (it["submittedAtMillis"] as? Number)?.toLong() ?: 0L }
        }
        val metricsListener = StreamerRepository.metrics.addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                clickMetrics = snapshot.documents.mapNotNull { doc ->
                    doc.getLong("clickCount")?.let { doc.id to it }
                }.toMap()
            }
        }
        onDispose { listener.remove(); metricsListener.remove() }
    }
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PanelReadControl(com.example.data.NotificationPanel.ADMINISTRATION)
        Text(localizedString(R.string.streamer_requirement), color = Color.White)
        Text(localizedString(R.string.streamer_count, entries.size), color = StreamGold)
        if (entries.size >= 5) Text(localizedString(R.string.streamer_max), color = StreamGold)
        operationError(result)?.let { Text(it, color = Color(0xFFFF8A80)) }
        if (!available || !requestAvailable) Text(localizedString(R.string.streamer_loading), color = Color.White)
        if (metricRepairFailed) Text(localizedString(R.string.streamer_history_clicks_unavailable), color = StreamGold)
        entries.forEach { item ->
            val pubId = StreamerPublicationPolicy.publicationId(item)
            val clicks = clickMetrics[pubId] ?: (item["clickCount"] as? Number)?.toLong() ?: 0L
            ApprovedStreamerReviewCard(
                item = item,
                enabled = !busy && available,
                onOpen = { url -> runCatching { uri.openUri(url) } },
                onEnd = { busy = true; scope.launch { result = StreamerRepository.end(item["userId"] as String); busy = false } },
                clicks = clicks
            )
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
                        val requestHours = StreamerPublicationPolicy.durationHours(request)
                        val requestDurationValue = if (requestHours <= 0) localizedString(R.string.streamer_duration_extensible) else localizedString(R.string.streamer_duration_hours, requestHours)
                        Text(localizedString(R.string.streamer_duration_selected, requestDurationValue), color = StreamGold, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.testTag("streamer_admin_duration_$uid"))
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

/** Approved channels remain available for verification after leaving the pending queue. */
@Composable
fun ApprovedStreamerReviewCard(item: Map<String, Any>, enabled: Boolean,
    onOpen: (String) -> Unit, onEnd: () -> Unit, clicks: Long = 0L) {
    val channel = StreamChannelUrl.approved(item["channelUrl"] as? String ?: "")
    val now = streamerClock()
    val durationHours = StreamerPublicationPolicy.durationHours(item)
    val durationValue = if (durationHours <= 0) localizedString(R.string.streamer_duration_extensible) else localizedString(R.string.streamer_duration_hours, durationHours)
    val liveDeadline = StreamerPublicationPolicy.liveExpiresAt(item)
    Surface(color = Color(0xFF1F2937), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item["channelName"] as? String ?: "", color = StreamGold, style = MaterialTheme.typography.titleMedium)
            Text(localizedString(R.string.streamer_review_accepted), color = Color(0xFF2DD4BF))
            Text(localizedString(R.string.streamer_duration_selected, durationValue), color = StreamGold)
            if (liveDeadline > 0L) {
                val remainingSeconds = ((liveDeadline - now).coerceAtLeast(0L) + 999L) / 1000L
                Text(localizedString(R.string.streamer_live_ends_in, remainingSeconds / 3600L, (remainingSeconds / 60L) % 60L, remainingSeconds % 60L), color = Color.White)
            }
            Text(item["channelUrl"] as? String ?: "", color = Color.White)
            Text(
                localizedString(R.string.streamer_history_clicks, clicks),
                color = StreamGold,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { channel?.let { onOpen(it.url) } }, enabled = channel != null) { Text(localizedString(R.string.streamer_open)) }
                TextButton(onClick = onEnd, enabled = enabled) { Text(localizedString(R.string.streamer_end)) }
            }
        }
    }
}
