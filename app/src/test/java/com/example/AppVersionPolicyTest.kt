package com.example

import com.example.util.AppVersionPolicy
import org.junit.Assert.*
import org.junit.Test

class AppVersionPolicyTest {
    @Test fun productionAndTestFormatsAreSeparated() {
        assertTrue(AppVersionPolicy.isProductionVersion("1.1.10.463"))
        assertTrue(AppVersionPolicy.isTestVersion("1.1.10.463-prueba.1"))
        assertEquals("1.1.10.463-prueba.7", AppVersionPolicy.testVersion("1.1.10.463", "7"))
        assertFalse(AppVersionPolicy.isProductionVersion("1.1.10.463-prueba.1"))
        assertFalse(AppVersionPolicy.isTestVersion("1.1.10.463-prueba"))
        assertNull(AppVersionPolicy.testVersion("1.1.10.463-prueba.1", "8"))
        assertNull(AppVersionPolicy.testVersion("1.1.10.463", "0"))
    }

    @Test fun onlyTheExplicitMinimumMakesAReleaseObsolete() {
        assertTrue(AppVersionPolicy.isObsolete(1239, 1240))
        assertFalse(AppVersionPolicy.isObsolete(1240, 1240))
        assertFalse(AppVersionPolicy.isObsolete(1240, 0))
        assertFalse(AppVersionPolicy.isObsolete(1240, -1))
    }
}
