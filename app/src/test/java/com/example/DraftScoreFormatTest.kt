package com.example

import com.example.data.DraftScoreFormat
import org.junit.Assert.*
import org.junit.Test

class DraftScoreFormatTest {
    @Test fun `score accepts a complete optional scoreboard and normalizes leading zeroes`() {
        assertEquals("", DraftScoreFormat.normalize(""))
        assertEquals("12/2/8", DraftScoreFormat.normalize("012/02/008"))
        for (invalid in listOf("12/2", "12/2/", "-1/2/3", "5.0 KDA", "victoria", "1/2/3/4", "1000/2/3")) {
            assertFalse(invalid, DraftScoreFormat.isValid(invalid))
            assertTrue(runCatching { DraftScoreFormat.normalize(invalid) }.isFailure)
        }
        assertFalse(DraftScoreFormat.acceptsInput("1/2/a"))
    }
}
