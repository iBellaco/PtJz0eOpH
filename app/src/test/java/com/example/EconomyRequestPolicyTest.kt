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
    @Test fun roleChangesCannotModifyBalancesOrAssignAdministrator() {
        val account=mapOf<String,Any>("role" to "creador", "secondaryRole" to "streamer", "blueEssence" to 99L,
            "orangeEssence" to 25L, "premiumUntil" to 9000000000000L, "subscriptionPlan" to "ADMIN_GIFT")
        val patch=com.example.data.AdminRolePolicy.patch(account,"free",1000L)
        assertEquals(0L,patch["premiumUntil"]); assertEquals("FREE",patch["subscriptionPlan"])
        assertFalse(patch.containsKey("orangeEssence"));assertFalse(patch.containsKey("blueEssence"));assertFalse(patch.containsKey("secondaryRole"))
        assertTrue(runCatching { com.example.data.AdminRolePolicy.patch(account,"admin",1000L) }.isFailure)
        assertEquals("",com.example.data.AdminRolePolicy.patch(account,"banned",1000L)["sessionToken"])
    }
    @Test fun assigningPremiumPreservesExistingTimeAndDoesNotExtendOnRetry() {
        val initial=mapOf<String,Any>("role" to "free", "premiumUntil" to 0L)
        val grant=com.example.data.AdminRolePolicy.patch(initial,"premium",1000L)
        assertEquals(1000L+30L*86400000L,grant["premiumUntil"])
        val retry=com.example.data.AdminRolePolicy.patch(initial+grant,"premium",2000L)
        assertFalse(retry.containsKey("premiumUntil"))
    }

}
