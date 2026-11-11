package com.example

import com.example.data.CalibrationReport
import com.example.data.CalibrationUserName
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test

class CalibrationReportTest {
    @Test fun titleIdentifiesTheSenderSubscriptionWithoutExposingIdentity() {
        assertEquals("Coordenadas del escáner · Usuario Premium", CalibrationReport.title(true, "es"))
        assertEquals("Coordenadas del escáner · Usuario gratuito", CalibrationReport.title(false, "es"))
        assertEquals("Coordenadas do scanner · Usuário Premium", CalibrationReport.title(true, "pt"))
        assertEquals("Coordenadas do scanner · Usuário gratuito", CalibrationReport.title(false, "pt"))
    }

    @Test fun exactCoordinatesSurviveSupportEnvelopeAndCopy() {
        val coordinates = VisionCalibrationConfig().copy(allyAvatarCenterX = .105f).toFormattedCoordinatesString()
        val report = CalibrationReport.build("model\ninjection", "1.1", "1080x2400", "2400x1080", coordinates, userName = "Tester")
        assertEquals(report, CalibrationReport.extract("Mensaje\n$report\nRespuesta"))
        assertTrue(report.contains(coordinates))
        assertTrue(report.contains("Dispositivo: model injection\n"))
        assertTrue(report.contains("Usuario: Tester\n"))
        assertTrue(report.contains("COORDENADAS\n"))
        assertFalse(report.contains("SOLO ADMINISTRACIÓN"))
        assertTrue(report.length < 16000)
        assertFalse(CalibrationReport.displayText(report).orEmpty().contains(CalibrationReport.MARKER))
    }
    @Test fun incompleteOrOversizedEnvelopesAreNotCopyActions() {
        assertNull(CalibrationReport.extract("Normal support message"))
        assertNull(CalibrationReport.extract(CalibrationReport.MARKER))
        assertNull(CalibrationReport.extract(CalibrationReport.MARKER + "x".repeat(16000) + CalibrationReport.END))
        val legacy = "[COACH_CALIBRATION_V1]\ndevice=old\n[/COACH_CALIBRATION_V1]"
        assertEquals(legacy, CalibrationReport.extract(legacy))
        val legacyVerbose = "[COACH_CALIBRATION_V1]\nETIQUETA: COORDENADAS DEL ESCÁNER — SOLO ADMINISTRACIÓN\nUsuario: Tester\nCaptura analizada: 1080x2400\nCoach: 1.0\n--- COORDENADAS GUARDADAS ---\nallySlotX=0.1f\n[/COACH_CALIBRATION_V1]"
        assertFalse(CalibrationReport.displayText(legacyVerbose).orEmpty().contains("SOLO ADMINISTRACIÓN"))
        assertFalse(CalibrationReport.displayText(legacyVerbose).orEmpty().contains("Captura analizada"))
    }
    @Test fun machineCoordinatesIncludeBothBanRowsAndAllIndividualSlots() {
        val config = VisionCalibrationConfig().copy(manualEnemyBans = true,
            enemyBanXRatios = listOf(.80f,.84f,.88f,.92f,.96f))
        val payload = CalibrationReport.build("model", "1.1", "1080x2400", "2400x1080", config.toKotlinCode())
        assertTrue(payload.contains("manualEnemyBans = true"))
        for (field in listOf("allySlotXRatios", "enemySlotXRatios", "allySlotYRatios", "enemySlotYRatios",
            "allySlotDiameterRatios", "enemySlotDiameterRatios", "topAllyXRatios", "topEnemyXRatios",
            "allyBanXRatios", "enemyBanXRatios", "allyBanYRatios", "enemyBanYRatios",
            "allyBanDiameterRatios", "enemyBanDiameterRatios")) assertTrue(field, payload.contains(field))
        assertEquals(payload, CalibrationReport.extract(payload))
    }

    @Test fun usernameNeverFallsBackToEmailIdentity() {
        assertEquals("Nombre Coach", CalibrationUserName.select(listOf("", "Nombre Coach", "correo@test.invalid")))
        assertEquals("Usuario", CalibrationUserName.select(listOf(null, "correo@test.invalid", "Invocador")))
    }

    @Test fun supportPayloadStaysCompactAndLocalizesPortugueseLabels() {
        val payload = CalibrationReport.build(
            device = "Xiaomi 14; API 35",
            app = "ignored",
            display = "1080x2400; density=3",
            capture = "ignored",
            coordinates = "allySlotX=0.1f",
            userName = "tester@example.invalid",
            language = "pt",
            androidVersion = "15 (API 35)"
        )
        assertTrue(payload.contains("Usuário: Usuário"))
        assertTrue(payload.contains("Tela: 1080x2400"))
        assertTrue(payload.contains("Android: 15 (API 35)"))
        assertFalse(payload.contains("Captura"))
        assertFalse(payload.contains("Coach:"))
        assertFalse(payload.contains("@"))
    }
}
