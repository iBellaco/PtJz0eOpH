package com.example.data

enum class UsdtNetwork { TRC20, ERC20, BEP20 }

object UsdtWalletPolicy {
    private val tokenContracts = setOf("txlaq63xg1nazckpwkhvzw7csemlmeqcdj", "0xdac17f958d2ee523a2206206994597c13d831ec7", "0x55d398326f99059ff775485246999027b3197955")
    fun valid(network: UsdtNetwork, wallet: String): Boolean {
        if (wallet != wallet.trim() || wallet.lowercase() in tokenContracts) return false
        if (network != UsdtNetwork.TRC20) return wallet.matches(Regex("0x[0-9a-fA-F]{40}")) && wallet.drop(2).any { it != '0' }
        if (!wallet.matches(Regex("T[1-9A-HJ-NP-Za-km-z]{33}"))) return false
        return runCatching {
            val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
            val decoded = wallet.fold(java.math.BigInteger.ZERO) { value, char -> value * java.math.BigInteger.valueOf(58) + java.math.BigInteger.valueOf(alphabet.indexOf(char).toLong()) }
                .toByteArray().let { if (it.size == 26 && it[0] == 0.toByte()) it.drop(1).toByteArray() else it }
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            decoded.size == 25 && decoded[0] == 0x41.toByte() &&
                digest.digest(digest.digest(decoded.take(21).toByteArray())).take(4) == decoded.takeLast(4)
        }.getOrDefault(false)
    }
}
