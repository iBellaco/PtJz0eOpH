package com.example

import com.example.util.DiagnosticPrivacy
import com.example.util.WebMediaPolicy
import org.junit.Assert.*
import org.junit.Test

class SecurityPrioritiesTest {
    @Test fun webAuthorityCannotBeSpoofedByPathsCredentialsOrSuffixes() {
        for (url in listOf("https://youtube.com.attacker.test/watch?v=12345678901",
            "https://attacker.test/youtube.com", "https://youtube.com@attacker.test/",
            "https://attacker.test/?next=https://youtube.com", "http://youtube.com/",
            "file:///youtube.com", "content://youtube.com", "javascript:alert(1)",
            "https://youtube.com:444/")) assertFalse(url, WebMediaPolicy.allowsVideo(url))
        for (url in listOf("https://youtube.com/watch?v=12345678901",
            "https://www.youtube-nocookie.com/embed/12345678901", "https://player.vimeo.com/video/1"))
            assertTrue(url, WebMediaPolicy.allowsVideo(url))
    }
    @Test fun redirectsStayWithinChosenProvider() {
        assertTrue(WebMediaPolicy.allowsNavigation("https://vimeo.com/1", "https://player.vimeo.com/video/1"))
        assertFalse(WebMediaPolicy.allowsNavigation("https://vimeo.com/1", "https://attacker.test/"))
        assertFalse(WebMediaPolicy.allowsNavigation("https://vimeo.com/1", "file:///private"))
    }
    @Test fun diagnosticsRedactSecretsAndBoundMemory() {
        val result = DiagnosticPrivacy.redact("mail=person@example.test token=secretvalue Bearer sensitivevalue https://example.test/a?key=privatevalue")
        for (secret in listOf("person@example.test", "secretvalue", "sensitivevalue", "privatevalue"))
            assertFalse(result.contains(secret))
        assertTrue(DiagnosticPrivacy.redact("x".repeat(20_000)).length <= 4096)
    }
}
