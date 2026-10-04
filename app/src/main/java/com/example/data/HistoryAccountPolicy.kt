package com.example.data

data class HistoryBalances(val blue: Long, val orange: Long)

/** A managed account must never inherit the viewer's wallet or purchase actions. */
object HistoryAccountPolicy {
    fun isOwnAccount(targetUid: String?, viewerUid: String?) =
        !targetUid.isNullOrBlank() && targetUid == viewerUid

    fun balances(profile: Map<String, Any>?): HistoryBalances? = profile?.let {
        HistoryBalances((it["blueEssence"] as? Number)?.toLong() ?: 0L,
            (it["orangeEssence"] as? Number)?.toLong() ?: 0L)
    }

    fun visibleBalances(ownAccount: Boolean, viewer: HistoryBalances, target: HistoryBalances?): HistoryBalances? =
        if (ownAccount) viewer else target
}
