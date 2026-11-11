package com.example.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayAccessSnapshotTest {
    @Test fun activationButtonIsEnabledByDefaultForExistingSettings() {
        assertTrue(OverlayAccessSnapshot().activationButtonEnabled)
    }

    @Test fun activationSwitchDoesNotChangeViewerAccess() {
        val snapshot = OverlayAccessSnapshot(
            hubButtonsVisible = true,
            allowedAccountIds = setOf("user-1"),
            activationButtonEnabled = false
        )
        assertFalse(snapshot.activationButtonEnabled)
        assertTrue(snapshot.hubButtonsVisibleFor("user-1"))
        assertTrue(snapshot.hubButtonsVisibleFor("user-2"))
        assertTrue(snapshot.activationEnabledFor("user-1"))
        assertFalse(snapshot.activationEnabledFor("user-2"))
        val disabled = snapshot.copy(hubButtonsVisible = false)
        assertTrue(disabled.hubButtonsVisibleFor("user-1"))
        assertFalse(disabled.hubButtonsVisibleFor("user-2"))
        assertFalse(disabled.hubButtonsVisibleFor(null))
    }

    @Test fun viewerDisabledDoesNotForceActivationDisabled() {
        val snapshot = OverlayAccessSnapshot(hubButtonsVisible = false)
        assertTrue(snapshot.activationButtonEnabled)
        assertFalse(snapshot.hubButtonsVisibleFor("user-1"))
    }
}
