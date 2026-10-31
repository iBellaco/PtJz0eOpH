package com.example.ui.auth

object PasswordPolicy {
    fun accepts(password: String): Boolean = password.length >= 8 &&
        password.any(Char::isUpperCase) && password.any(Char::isLowerCase) &&
        password.any(Char::isDigit) && password.any { !it.isLetterOrDigit() && !it.isWhitespace() } &&
        password.none(Char::isWhitespace)
}
