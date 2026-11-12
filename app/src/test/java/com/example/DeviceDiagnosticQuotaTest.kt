package com.example

import android.app.Application
import com.example.data.DeviceDiagnosticQuota
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DeviceDiagnosticQuotaTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Before
    fun clearQuota() {
        context.getSharedPreferences("device_diagnostic_quota", 0).edit().clear().commit()
    }

    @Test
    fun allowsFiveSuccessfulReservationsAndBlocksTheSixth() {
        repeat(DeviceDiagnosticQuota.MAX_PER_DAY) {
            assertNotNull(DeviceDiagnosticQuota.tryReserve(context))
        }
        assertNull(DeviceDiagnosticQuota.tryReserve(context))
        assertEquals(0, DeviceDiagnosticQuota.usage(context).remaining)
    }

    @Test
    fun failedSubmissionCanReturnItsReservedSlot() {
        val reservation = DeviceDiagnosticQuota.tryReserve(context)
        assertNotNull(reservation)
        assertEquals(4, DeviceDiagnosticQuota.usage(context).remaining)
        DeviceDiagnosticQuota.rollback(context, reservation!!)
        assertEquals(DeviceDiagnosticQuota.MAX_PER_DAY, DeviceDiagnosticQuota.usage(context).remaining)
    }
}
