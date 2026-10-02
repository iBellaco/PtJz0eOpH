package com.example

import android.app.Application
import com.example.data.*
import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24], application = Application::class)
class SupportReportDecoderTest {
    private val time = 1_790_899_200_123L

    @Test fun `legacy content dates version and device remain readable`() {
        val report = SupportReportDecoder.decode("legacy", mapOf(
            "subject" to "Ajuda", "content" to "Preciso de ajuda", "created_at" to "2026-10-02T00:00:00.123-04:00",
            "app_version" to "1.1.10.146", "device_info" to "Android 14", "user_id" to "owner"
        ))!!
        assertEquals("Ajuda", report.title)
        assertEquals("Preciso de ajuda", report.description)
        assertEquals("2026-10-02T04:00:00.123Z", report.createdAt)
        assertEquals("1.1.10.146", report.appVersion)
        assertEquals("Android 14", report.deviceInfo)
        assertEquals("owner", report.userId)
    }

    @Test fun `blank canonical fields fall back to populated legacy fields`() {
        val report = SupportReportDecoder.decode("fallback", mapOf(
            "title" to "", "subject" to "Assunto", "description" to "", "content" to "Mensagem",
            "createdAt" to 0L, "timestamp" to time, "appVersion" to "", "app_version" to "147",
            "deviceInfo" to "", "device" to "Pixel"
        ))!!
        assertEquals("Mensagem", report.description)
        assertEquals(time, SupportReportDecoder.timestampMillis(report.createdAt))
        assertEquals("147", report.appVersion)
        assertEquals("Pixel", report.deviceInfo)
    }

    @Test fun `timestamp types and offset ISO dates preserve the instant on Android 24`() {
        val iso = SupportReportDecoder.isoDate(time)
        listOf<Any>(time, time.toString(), java.util.Date(time), Timestamp(java.util.Date(time)),
            mapOf("seconds" to time / 1000, "nanoseconds" to 123_000_000), iso!!).forEach {
            assertEquals(it.toString(), time, SupportReportDecoder.timestampMillis(it))
        }
        assertEquals(1_790_899_200_000L, SupportReportDecoder.timestampMillis("2026-10-02T00:00:00Z"))
        assertNull(SupportReportDecoder.timestampMillis("2026-02-30T00:00:00Z"))
        assertNull(SupportReportDecoder.timestampMillis("invalid"))
        assertNull(SupportReportDecoder.timestampMillis(0))
    }

    @Test fun `conversation-only tickets recover the initial message and timestamp`() {
        val history = SupportConversationPolicy.initial("history", "Tester", "Olá", time)
        val report = SupportReportDecoder.decode("history", mapOf(
            "conversation" to history.map { SupportConversationPolicy.encode(it) }
        ))!!
        assertEquals("Olá", report.description)
        assertEquals("Tester", report.userName)
        assertEquals(time, SupportReportDecoder.timestampMillis(report.createdAt))
        assertNull(SupportReportDecoder.decode("partial", mapOf("status" to "PENDING", "userRead" to true)))
    }

    @Test fun `unknown date never becomes epoch or expires`() {
        val report = SupportReportDecoder.decode("unknown", mapOf("title" to "Ajuda", "content" to "Olá"))!!
        assertNull(report.createdAt)
        val countdown = SupportReplyManager.calculateCountdown(SupportReplyManager.parseDateToMillis(report.createdAt), false)
        assertFalse(countdown.isExpired)
        assertEquals("Fecha no disponible", countdown.displayText)
    }
}
