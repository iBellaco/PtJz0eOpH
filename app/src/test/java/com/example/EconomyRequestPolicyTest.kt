package com.example

import com.example.data.EconomyRequestPolicy
import org.junit.Assert.*
import org.junit.Test

class EconomyRequestPolicyTest {
    @Test fun retriesReuseOnlyTheSameMeaning() {
        val existing = mapOf("id" to "old", "action" to "ROLE", "uid" to "a", "role" to "free")
        assertTrue(EconomyRequestPolicy.sameCommand(existing, existing + ("id" to "new")))
        assertFalse(EconomyRequestPolicy.sameCommand(existing, existing + ("role" to "premium")))
        assertFalse(EconomyRequestPolicy.sameCommand(existing, existing + ("uid" to "b")))
        assertFalse(EconomyRequestPolicy.sameCommand(existing, existing + ("action" to "PREMIUM_REMOVE")))
    }
    @Test fun numericSerializationAndMapOrderDoNotCauseASecondPayment() {
        val existing = mapOf("id" to "old", "action" to "SPONSOR", "notice" to mapOf("duration" to 2L, "title" to "Test"))
        assertTrue(EconomyRequestPolicy.sameCommand(existing, mapOf("action" to "SPONSOR", "id" to "new", "notice" to mapOf("title" to "Test", "duration" to 2))))
        assertFalse(EconomyRequestPolicy.sameCommand(existing, existing + ("notice" to mapOf("duration" to 3, "title" to "Test"))))
    }
}
