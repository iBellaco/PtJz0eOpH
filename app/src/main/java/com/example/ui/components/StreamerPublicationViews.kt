package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.StreamerPublicationPolicy
import com.example.util.currentAppLanguage
import com.example.util.localizedString
import java.text.DateFormat
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

@Composable
fun LiveStreamerChip(channelName: String, onClick: () -> Unit) {
    val liveDescription = localizedString(R.string.streamer_live_description)
    val transition = rememberInfiniteTransition(label = "streamer-live")
    val glow by transition.animateFloat(0.35f, 1f,
        infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "live-indicator")
    AssistChip(onClick = onClick, modifier = Modifier.testTag("live_streamer_chip").semantics { stateDescription = liveDescription },
        leadingIcon = {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(16.dp).alpha(glow * 0.4f).background(Color(0xFF34D399), CircleShape))
                Box(Modifier.size(8.dp).alpha(glow).background(Color(0xFF34D399), CircleShape))
            }
        }, label = { Text(channelName, color = Color(0xFFD4AF37)) })
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
fun StreamerPublicationHistory(publications: List<Map<String, Any>>, now: Long) {
    val locale = if (currentAppLanguage() == "pt") Locale("pt", "BR") else Locale("es", "ES")
    val dayFormat = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
    val timeFormat = remember(locale) { DateFormat.getTimeInstance(DateFormat.SHORT, locale) }
    val days = publications.distinctBy { StreamerPublicationPolicy.publicationId(it) }
        .sortedByDescending(StreamerPublicationPolicy::submittedAt).groupBy {
            val date = StreamerPublicationPolicy.submittedAt(it)
            if (date > 0) dayFormat.format(Date(date)) else ""
        }
    Column(Modifier.fillMaxWidth().testTag("streamer_publication_history"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(localizedString(R.string.streamer_history), style = MaterialTheme.typography.titleMedium, color = Color(0xFFD4AF37))
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (timestamp > 0L) Text(timeFormat.format(Date(timestamp)), color = Color.LightGray)
                            Text(localizedString(statusId), color = color)
                        }
                        if (item["rejectionReason"] == "TIMEOUT" || StreamerPublicationPolicy.isExpired(item, now)) {
                            Text(localizedString(R.string.streamer_expired), color = Color.LightGray)
                        }
                        if (status == "ENDED") Text(localizedString(R.string.streamer_history_ended), color = Color.LightGray)
                    }
                }
            }
        }
    }
}
