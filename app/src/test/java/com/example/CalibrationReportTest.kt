package com.example

import com.example.data.CalibrationReport
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test

class CalibrationReportTest {
    @Test fun exactCoordinatesSurviveSupportEnvelopeAndCopy() {
        val coordinates = VisionCalibrationConfig().copy(allyAvatarCenterX = .105f).toFormattedCoordinatesString()
        val report = CalibrationReport.build("model\ninjection", "1.1", "1080x2400", "2400x1080", coordinates)
        assertEquals(report, CalibrationReport.extract("Mensaje\n$report\nRespuesta"))
        assertTrue(report.contains(coordinates))
        assertTrue(report.contains("device=model injection\n"))
        assertTrue(report.length < 16000)
    }
    @Test fun incompleteOrOversizedEnvelopesAreNotCopyActions() {
        assertNull(CalibrationReport.extract("Normal support message"))
        assertNull(CalibrationReport.extract(CalibrationReport.MARKER))
        assertNull(CalibrationReport.extract(CalibrationReport.MARKER + "x".repeat(16000) + CalibrationReport.END))
    }
}
