package com.example

import com.example.ui.theme.CoachNavStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class CoachNavStyleTest {
    @Test fun everyStyleRoundTripsThroughPersistedId() {
        CoachNavStyle.entries.forEach { style ->
            assertEquals(style, CoachNavStyle.fromId(style.id))
        }
    }

    @Test fun removedNotchedStylesRecoverTheOriginalBar() {
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("curved_fab"))
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("curved_scoop"))
    }

    @Test fun unknownPersistedStyleFallsBackToTheStableDefault() {
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("missing"))
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId(null))
    }

    @Test fun referenceVideoStylesArePersistable() {
        listOf(
            CoachNavStyle.GLASS_PILL,
            CoachNavStyle.TABS_V2,
            CoachNavStyle.PURPLE_MESSAGES,
            CoachNavStyle.MENISCUS,
            CoachNavStyle.VIDEO_LIQUID_GLASS,
            CoachNavStyle.VIDEO_DARK_NEON_LENS,
            CoachNavStyle.VIDEO_BOUNCE,
            CoachNavStyle.VIDEO_NEON_WAVE,
            CoachNavStyle.VIDEO_SPOTLIGHT,
            CoachNavStyle.VIDEO_FLOATING_BUTTON,
            CoachNavStyle.GLASS_WAVE,
            CoachNavStyle.GLASS_CONE,
            CoachNavStyle.GLASS_BEAD,
            CoachNavStyle.WARM_TABS,
            CoachNavStyle.RIBBON_MATRIX,
            CoachNavStyle.PRISM_LATTICE
        ).forEach { style ->
            assertEquals(style, CoachNavStyle.fromId(style.id))
        }
    }

    @Test fun retiredNavigationStyleFallsBackWithoutRemovingRegionalThemes() {
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("premium_menu"))
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("dark_neon_lines"))
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("spotlight"))
        assertEquals(com.example.ui.theme.AppTheme.ZAUN, com.example.ui.theme.AppTheme.fromId("ZAUN"))
    }

    @Test fun navigationCatalogHasTwentySixStylesAfterRetiringTwoEntries() {
        assertEquals(26, CoachNavStyle.entries.size)
    }
}
