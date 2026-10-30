package com.example

import com.example.data.*
import com.example.model.*
import com.example.util.CoachingGenerator
import org.junit.Assert.*
import org.junit.Test

class AppReliabilityTest {
    @Test fun operationIdentitySurvivesRestartAndSeparatesAccountsTargetsAndAmounts() {
        val disk = mutableMapOf<String, String>()
        fun restart() = PendingOperationIds({ disk[it] }, { k, v -> disk[k] = v; true }, { disk.remove(it); true })
        val fields = mapOf<String, Any>("uid" to "recipient", "amount" to 500L, "currency" to "BLUE")
        val key = EconomyRequestPolicy.operationKey("owner", "ADJUST", fields)
        assertEquals("first", restart().reserve(key, "first"))
        assertEquals("first", restart().reserve(key, "retry"))
        assertNotEquals(key, EconomyRequestPolicy.operationKey("other", "ADJUST", fields))
        assertNotEquals(key, EconomyRequestPolicy.operationKey("owner", "ADJUST", fields + ("amount" to 100)))
        assertNotEquals(key, EconomyRequestPolicy.operationKey("owner", "ADJUST", fields + ("uid" to "other")))
        assertEquals(key, EconomyRequestPolicy.operationKey("owner", "ADJUST", fields + ("amount" to 500.0)))
        restart().adopt(key, "legacy")
        assertEquals("legacy", restart().reserve(key, "ignored"))
        restart().complete(key)
        assertEquals("new", restart().reserve(key, "new"))
    }

    @Test fun failureToPersistNeverReturnsAnIdForSending() {
        val ids = PendingOperationIds({ null }, { _, _ -> false }, { false })
        assertThrows(IllegalStateException::class.java) { ids.reserve("command", "id") }
    }

    @Test fun tenVisibleChampionsAreNotAutomaticallyConfirmed() {
        assertEquals(DraftCertainty.PROVISIONAL, DraftCertaintyPolicy.selection(false, true, 10))
        assertEquals(DraftCertainty.COLLECTING, DraftCertaintyPolicy.selection(false, false, 10))
        assertEquals(DraftCertainty.CONFIRMED, DraftCertaintyPolicy.selection(true, true, 10))
        assertEquals(DraftCertainty.COLLECTING, DraftCertaintyPolicy.selection(true, true, 9))
    }

    @Test fun emailAndRoleLabelsCannotGrantAdministrationAndBansOverrideClaims() {
        assertFalse(RolePanelAccess.canOpen(RolePanel.ADMINISTRATION, "admin"))
        assertTrue(RolePanelAccess.canOpen(RolePanel.ADMINISTRATION, "free", adminClaim = true))
        assertTrue(RolePanelAccess.canOpen(RolePanel.SPONSOR, "free", adminClaim = true))
        assertFalse(RolePanelAccess.canOpen(RolePanel.ADMINISTRATION, "free", "banned", true))
        assertFalse(RolePanelAccess.canRedeemEssence("free", " BANNED ", true))
        assertTrue(RolePanelAccess.canOpen(RolePanel.SPONSOR, " SPONSOR "))
        assertTrue(RolePanelAccess.canOpen(RolePanel.SPONSOR, "free", "patrocinio"))
        assertTrue(RolePanelAccess.canOpen(RolePanel.MODERATION, "moderator"))
    }

    @Test fun missingMageTopBuildIsNotFilledWithPhysicalFighterItems() {
        val mage = Champion(id = "test_mage", name = "Test", primaryRole = LaneRole.TOP, damageType = DamageType.MAGIC)
        val plan = DraftBuildPolicy.create(mage, LaneRole.TOP, emptyList(), emptyList())
        assertTrue(plan.coreItems.isEmpty())
        assertTrue(plan.runes.isEmpty())
        assertTrue(plan.reasons.any { it.contains("Base incompleta") })
        assertTrue(DraftBuildPolicy.create(mage, LaneRole.TOP, emptyList(), emptyList(), lang = "pt")
            .reasons.any { it.contains("este campeão e rota") })
    }

    @Test fun supportNeverReceivesTwoQuestItemsOrTwoKeystones() {
        val support = Champion(id = "test_support", name = "Test", primaryRole = LaneRole.SUPPORT,
            coreItems = listOf("Hoz espectral", "Baluarte de la montaña", "Escudo reliquia"),
            recommendedRunes = "Conquistador, Electrocutar")
        val plan = DraftBuildPolicy.create(support, LaneRole.SUPPORT, emptyList(), emptyList())
        assertEquals(listOf("Hoz espectral"), plan.coreItems)
        assertTrue(plan.runes.isEmpty())
    }

    @Test fun draftAdviceStatesUncertaintyAndChangesWithTheActualRival() {
        val champion = Champion(id = "own", name = "Own", tacticalAdvice = "Conserva tu control para proteger la salida.")
        val first = CoachingGenerator.generateTacticalAdvice(champion, LaneRole.JUNGLE, "es", Champion(id = "one", name = "One"))
        val second = CoachingGenerator.generateTacticalAdvice(champion, LaneRole.JUNGLE, "es", Champion(id = "two", name = "Two"))
        assertNotEquals(first, second)
        assertTrue(first.contains("no observamos vida"))
        assertTrue(first.contains("prioridad de las dos líneas"))
        val pt = CoachingGenerator.generateTacticalAdvice(champion, LaneRole.JUNGLE, "pt")
        assertTrue(pt.contains("não observamos vida"))
        assertFalse(pt.contains("Plan condicionado"))
    }
}
