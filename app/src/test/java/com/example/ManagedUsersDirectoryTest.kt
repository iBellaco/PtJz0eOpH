package com.example

import com.example.data.ManagedUsersDirectory
import org.junit.Assert.*
import org.junit.Test

class ManagedUsersDirectoryTest {
    @Test fun onlyAuthoritativelyMissingIdentitiesAreHidden() {
        assertFalse(ManagedUsersDirectory.isVisible(mapOf("authAccountExists" to false)))
        assertTrue(ManagedUsersDirectory.isVisible(mapOf("authAccountExists" to true)))
        assertTrue(ManagedUsersDirectory.isVisible(mapOf("role" to "free")))
        assertTrue(ManagedUsersDirectory.isVisible(mapOf("emailVerified" to false)))
    }
}
