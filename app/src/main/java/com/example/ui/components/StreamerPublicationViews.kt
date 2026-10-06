package com.example.ui.components

import com.example.ui.components.CoachAssistChip as AssistChip

import com.example.ui.components.CoachTextButton as TextButton

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.StreamerPublicationPolicy
import com.example.util.currentAppLanguage
import com.example.util.localizedString
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun streamerClock(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            now = System.currentTimeMillis()
        }
    }
    return now
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun LiveStreamerChip(channelName: String, onClick: () -> Unit) {
    val liveDescription = localizedString(R.string.streamer_live_description)
    val red = Color(0xFFFF6B6B)
    AssistChip(onClick = onClick, modifier = Modifier.heightIn(min = 54.dp).testTag("live_streamer_chip").semantics { stateDescription = liveDescription; testTagsAsResourceId = true },
        leadingIcon = {
            LiveStreamerIndicator(red)
        }, label = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(channelName, modifier = Modifier.testTag("streamer_channel_name"), color = Color(0xFFD4AF37), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(localizedString(R.string.streamer_live_label), color = red,
                    fontSize = 10.sp, modifier = Modifier.testTag("streamer_live_label"))
            }
        })
}

@Composable
fun StreamerSubmissionFeedback(busy: Boolean, submitted: Boolean) {
    if (busy || submitted) {
        Surface(color = Color(0xFF064E3B), shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("streamer_submission_feedback")) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (busy) CircularProgressIndicator(Modifier.size(18.dp), color = Color(0xFF34D399), strokeWidth = 2.dp)
                Text(localizedString(if (busy) R.string.streamer_sending else R.string.streamer_submitted), color = Color.White)
            }
        }
    }
}

@Composable
fun StreamerPublicationHistory(publications: List<Map<String, Any>>, now: Long, onCopy: ((String) -> Unit)? = null) {
    val locale = if (currentAppLanguage() == "pt") Locale("pt", "BR") else Locale("es", "ES")
    val dayFormat = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
    val exactFormat = remember(locale) { SimpleDateFormat("dd/MM/yyyy HH:mm:ss z", locale) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedMessage = localizedString(R.string.streamer_history_copied)
    val visible = publications.filterNot { StreamerPublicationPolicy.historyExpired(it, now) }
    val days = remember(visible, locale) {
        visible.distinctBy { StreamerPublicationPolicy.publicationId(it) }
            .sortedByDescending(StreamerPublicationPolicy::submittedAt).groupBy {
                val date = StreamerPublicationPolicy.submittedAt(it)
                if (date > 0) dayFormat.format(Date(date)) else ""
            }
    }
    Column(Modifier.fillMaxWidth().testTag("streamer_publication_history"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(localizedString(R.string.streamer_history), style = MaterialTheme.typography.titleMedium, color = Color(0xFFD4AF37))
        Text(localizedString(R.string.streamer_history_retention), color = Color.LightGray)
        if (days.isEmpty()) Text(localizedString(R.string.streamer_history_empty), color = Color.LightGray)
        days.forEach { (date, records) ->
            Text(date.ifBlank { localizedString(R.string.streamer_history_date_unavailable) }, color = Color.LightGray)
            records.forEach { item ->
                val status = StreamerPublicationPolicy.historyStatus(item, now)
                val statusId = when (status) {
                    "APPROVED", "ENDED" -> R.string.streamer_history_approved
                    "REJECTED" -> R.string.streamer_history_rejected
                    else -> R.string.streamer_history_pending
                }
                val color = when (status) {
                    "APPROVED", "ENDED" -> Color(0xFF34D399)
                    "REJECTED" -> Color(0xFFFF8A80)
                    else -> Color(0xFFD4AF37)
                }
                Surface(color = Color(0xFF1F2937), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.35f))) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text((item["channelName"] as? String).orEmpty(), color = Color.White)
                        val timestamp = StreamerPublicationPolicy.submittedAt(item)
                        val channel = (item["channelName"] as? String).orEmpty()
                        val exactDate = if (timestamp > 0L) exactFormat.format(Date(timestamp)) else localizedString(R.string.streamer_history_date_unavailable)
                        val dateText = localizedString(R.string.streamer_history_exact_date, exactDate)
                        val statusText = localizedString(statusId)
                        val clickText = (item["clickCount"] as? Number)?.let { localizedString(R.string.streamer_history_clicks, it.toLong()) }
                            ?: if (status in listOf("APPROVED", "ENDED")) localizedString(R.string.streamer_history_clicks, 0L)
                            else localizedString(R.string.streamer_history_clicks_unavailable)
                        val durationHours = StreamerPublicationPolicy.durationHours(item)
                        val durationValue = if (durationHours <= 0) localizedString(R.string.streamer_duration_extensible) else localizedString(R.string.streamer_duration_hours, durationHours)
                        val durationText = localizedString(R.string.streamer_duration_selected, durationValue)
                        val liveDeadline = if (status == "APPROVED") StreamerPublicationPolicy.liveExpiresAt(item) else 0L
                        val liveRemainingMillis = (liveDeadline - now).coerceAtLeast(0L)
                        val liveRemainingSeconds = (liveRemainingMillis + 999L) / 1000L
                        val liveCountdownText = if (liveDeadline > 0L) localizedString(
                            R.string.streamer_live_ends_in,
                            liveRemainingSeconds / 3600L,
                            (liveRemainingSeconds / 60L) % 60L,
                            liveRemainingSeconds % 60L
                        ) else ""
                        val liveCountdownColor = when {
                            liveRemainingMillis <= 10 * 60 * 1000L -> Color(0xFFFF5252)
                            liveRemainingMillis <= 30 * 60 * 1000L -> Color(0xFFFFA726)
                            else -> Color(0xFF22D3EE)
                        }
                        val deadline = StreamerPublicationPolicy.historyExpiresAt(item)
                        val remaining = ((deadline - now).coerceAtLeast(0L) + 59999L) / 60000L
                        val expiresText = if (deadline > 0L) localizedString(R.string.streamer_history_delete_in, remaining / 60L, remaining % 60L) else if (status == "APPROVED") localizedString(R.string.streamer_history_active_retention) else ""
                        Text(dateText, color = Color.LightGray)
                        Text(durationText, color = Color.White, modifier = Modifier.testTag("streamer_history_duration_${StreamerPublicationPolicy.publicationId(item)}"))
                        if (liveCountdownText.isNotBlank()) {
                            Text(
                                liveCountdownText,
                                color = liveCountdownColor,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                modifier = Modifier.testTag("streamer_history_live_countdown_${StreamerPublicationPolicy.publicationId(item)}")
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(statusText, color = color)
                        }
                        Text(clickText, color = Color.White, modifier = Modifier.testTag("streamer_clicks_${StreamerPublicationPolicy.publicationId(item)}"))
                        if (item["clicksLive"] == false) Text(localizedString(R.string.streamer_clicks_cached), color = Color.LightGray)
                        if (expiresText.isNotBlank()) {
                            val deletionRemaining = (deadline - now).coerceAtLeast(0L)
                            val deletionColor = if (deadline <= 0L) Color.LightGray else when {
                                deletionRemaining <= 6 * 60 * 60 * 1000L -> Color(0xFFFF5252)
                                deletionRemaining <= 24 * 60 * 60 * 1000L -> Color(0xFFFFA726)
                                else -> Color(0xFFA78BFA)
                            }
                            Text(expiresText, color = deletionColor, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                modifier = Modifier.testTag("streamer_history_delete_countdown_${StreamerPublicationPolicy.publicationId(item)}"))
                        }
                        if (item["rejectionReason"] == "TIMEOUT" || StreamerPublicationPolicy.isExpired(item, now)) {
                            Text(localizedString(R.string.streamer_expired), color = Color.LightGray)
                        }
                        if (status == "ENDED") Text(localizedString(R.string.streamer_history_ended), color = Color.LightGray)
                        val deletionDate = if (deadline > 0L) localizedString(R.string.streamer_history_deletion_date, exactFormat.format(Date(deadline))) else ""
                        val copyLabel = localizedString(R.string.streamer_history_copy)
                        TextButton(onClick = {
                            val summary = listOf(channel, dateText, durationText, liveCountdownText, statusText, clickText, expiresText, deletionDate).filter { it.isNotBlank() }.joinToString("\n")
                            if (onCopy != null) onCopy(summary) else {
                                clipboard.setText(AnnotatedString(summary))
                                android.widget.Toast.makeText(context, copiedMessage, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }, modifier = Modifier.testTag("streamer_history_copy_${StreamerPublicationPolicy.publicationId(item)}")) {
                            Text(copyLabel, color = Color(0xFFD4AF37))
                        }
                    }
                }
            }
        }
    }
}

/** Animate only the small indicator; channel text and chip layout remain stable. */
@Composable
private fun LiveStreamerIndicator(red: Color) {
    val transition = rememberInfiniteTransition(label = "streamer-live")
    val wave = transition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart), label = "live-wave")
    val frameWave = wave.value
            Canvas(Modifier.size(28.dp).testTag("streamer_live_animation")) {
                val point = Offset(size.width / 2, size.height / 2)
                repeat(2) { index ->
                    val progress = (frameWave + index * 0.5f) % 1f
                    drawCircle(red.copy(alpha = (1f - progress) * 0.85f),
                        radius = (4f + progress * 9f).dp.toPx(), center = point, style = Stroke(1.6.dp.toPx()))
                }
                drawCircle(red, radius = 4.dp.toPx(), center = point)
            }
}
