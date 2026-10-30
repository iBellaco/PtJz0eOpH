package com.example.data

/** Storage must acknowledge a durable write before any request leaves the device. */
internal class PendingOperationIds(
    private val read: (String) -> String?,
    private val write: (String, String) -> Boolean,
    private val remove: (String) -> Boolean
) {
    @Synchronized fun reserve(key: String, proposed: String): String {
        read(key)?.let { return it }
        check(write(key, proposed)) { "operation_storage_unavailable" }
        return proposed
    }

    // Failure to clear keeps the old key (safe to recover); it cannot undo a confirmed result.
    @Synchronized fun complete(key: String): Boolean = remove(key)

    @Synchronized fun adopt(key: String, id: String) {
        check(write(key, id)) { "operation_storage_unavailable" }
    }
}
