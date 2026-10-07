package com.example

import com.example.data.AccountDeletionPolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class AccountDeletionPolicyTest {
    @Test fun `recovery uses sixty elapsed days across the leap day`() {
        val start=Instant.parse("2024-02-01T15:30:00Z").toEpochMilli()
        assertEquals(Instant.parse("2024-04-01T15:30:00Z").toEpochMilli(),AccountDeletionPolicy.deadline(start))
    }
    @Test(expected=ArithmeticException::class) fun `invalid overflow cannot show a fabricated date`() {
        AccountDeletionPolicy.deadline(Long.MAX_VALUE)
    }
}
