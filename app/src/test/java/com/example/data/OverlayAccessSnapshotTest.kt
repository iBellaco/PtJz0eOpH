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
        assertFalse(snapshot.hubButtonsVisibleFor("user-2"))
    }

    @Test fun viewerDisabledDoesNotForceActivationDisabled() {
        val snapshot = OverlayAccessSnapshot(hubButtonsVisible = false)
        assertTrue(snapshot.activationButtonEnabled)
        assertFalse(snapshot.hubButtonsVisibleFor("user-1"))
    }
}
