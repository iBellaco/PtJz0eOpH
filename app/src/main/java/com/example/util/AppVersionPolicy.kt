package com.example.util

/** Rules shared by update checks and tests for internal and production builds. */
object AppVersionPolicy {
    private val productionPattern = Regex("^\\d+(?:\\.\\d+){2,3}$")
    private val testPattern = Regex("^\\d+(?:\\.\\d+){2,3}-prueba\\.[1-9]\\d*$", RegexOption.IGNORE_CASE)

    fun isTestVersion(name: String): Boolean = testPattern.matches(name.trim())

    fun isProductionVersion(name: String): Boolean = productionPattern.matches(name.trim())

    /**
     * Returns true only when the installed build is below the administrator's
     * explicit obsolescence threshold. A zero threshold means that no build is
     * obsolete. Keeping this rule numeric makes ordinary production releases
     * non-blocking: publishing a newer APK alone never traps an older client.
     */
    fun isObsolete(currentCode: Int, minimumSupportedCode: Int): Boolean =
        minimumSupportedCode > 0 && currentCode >= 0 && currentCode < minimumSupportedCode

    fun testVersion(baseName: String, iteration: String): String? {
        if (!productionPattern.matches(baseName.trim())) return null
        if (!Regex("^[1-9]\\d*$").matches(iteration.trim())) return null
        return "${baseName.trim()}-prueba.${iteration.trim()}"
    }
}
