package com.example.data

/** The server timestamp starts the recovery window, never the phone clock. */
object AccountDeletionPolicy {
    const val GRACE_DAYS = 60L
    const val GRACE_MILLIS = GRACE_DAYS * 24 * 60 * 60 * 1000
    fun deadline(requestedAtMillis: Long): Long = Math.addExact(requestedAtMillis, GRACE_MILLIS)
}
