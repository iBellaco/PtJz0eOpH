package com.example

import com.example.data.ManagedUsersDirectory
import org.junit.Assert.*
import org.junit.Test

class ManagedUsersDirectoryTest {
    @Test fun publicBuildHistoryDoesNotRequireAccessToPrivateAccounts() {
        assertTrue(ManagedUsersDirectory.creatorCardVisible(false, null))
        assertFalse(ManagedUsersDirectory.creatorCardVisible(true, null))
        assertFalse(ManagedUsersDirectory.creatorCardVisible(true, mapOf("authAccountExists" to false)))
        assertTrue(ManagedUsersDirectory.creatorCardVisible(true, mapOf("authAccountExists" to true)))
    }
    @Test fun offlineIsNotAnEmptyLiveDirectoryAndReconnectDropsDeletedAccounts() {
        val initial = ManagedUsersDirectory.State()
        assertEquals(ManagedUsersDirectory.Status.SYNCING, initial.status)
        val live = ManagedUsersDirectory.transition(initial, false,
            listOf(mapOf("uid" to "active"), mapOf("uid" to "removed", "authAccountExists" to false)), 123L)
        assertEquals(listOf("active"), live.users.map { it["uid"] })
        val offline = ManagedUsersDirectory.transition(live, true, live.users, 456L)
        assertEquals(ManagedUsersDirectory.Status.OFFLINE, offline.status)
        assertTrue(offline.users.isEmpty())
        assertEquals(123L, offline.lastSyncAt)
        val empty = ManagedUsersDirectory.transition(offline, false, emptyList(), 789L)
        assertEquals(ManagedUsersDirectory.Status.LIVE, empty.status)
        assertEquals(789L, empty.lastSyncAt)
        assertTrue(empty.users.isEmpty())
    }
    @Test fun onlyAuthoritativelyMissingIdentitiesAreHidden() {
        assertFalse(ManagedUsersDirectory.isVisible(mapOf("authAccountExists" to false)))
        assertTrue(ManagedUsersDirectory.isVisible(mapOf("authAccountExists" to true)))
        assertTrue(ManagedUsersDirectory.isVisible(mapOf("role" to "free")))
        assertTrue(ManagedUsersDirectory.isVisible(mapOf("emailVerified" to false)))
    }
}
