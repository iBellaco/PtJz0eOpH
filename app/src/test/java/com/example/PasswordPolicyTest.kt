package com.example

import com.example.ui.auth.PasswordPolicy
import org.junit.Assert.*
import org.junit.Test

class PasswordPolicyTest {
    @Test fun allAdvertisedRequirementsAreMandatory() {
        for (password in listOf("Example123", "example123!", "EXAMPLE123!", "Example!!", "Ex1!", "Example123 ")) {
            assertFalse(PasswordPolicy.accepts(password))
        }
        assertTrue(PasswordPolicy.accepts("Example123!"))
        assertTrue(PasswordPolicy.accepts("Abcdef1!"))
    }
}
