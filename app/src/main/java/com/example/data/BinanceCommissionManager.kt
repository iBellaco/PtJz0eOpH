package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Sistema de sincronización automática y en tiempo real de comisiones de red USDT Binance.
 * Se actualiza en segundo plano y escucha cambios directos de configuración para reflejar
 * de forma reactiva cualquier variación en la comisión sin necesidad de acción del usuario.
 */
object BinanceCommissionManager {

    private val defaultFees = mapOf(
        UsdtNetwork.BEP20 to 1L,
        UsdtNetwork.TRC20 to 2L,
        UsdtNetwork.ERC20 to 5L
    )

    private val _liveFees = MutableStateFlow(defaultFees)
    val liveFees: StateFlow<Map<UsdtNetwork, Long>> = _liveFees.asStateFlow()

    private val _lastUpdateMillis = MutableStateFlow(System.currentTimeMillis())
    val lastUpdateMillis: StateFlow<Long> = _lastUpdateMillis.asStateFlow()

    private var pollJob: Job? = null
    private var isInitialized = false

    fun init() {
        if (isInitialized) return
        isInitialized = true

        // Escucha en tiempo real de Firestore
        runCatching {
            FirebaseFirestore.getInstance()
                .collection("app_config")
                .document("binance_fees")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val updated = defaultFees.toMutableMap()
                        snapshot.getLong("BEP20")?.let { updated[UsdtNetwork.BEP20] = it }
                        snapshot.getLong("TRC20")?.let { updated[UsdtNetwork.TRC20] = it }
                        snapshot.getLong("ERC20")?.let { updated[UsdtNetwork.ERC20] = it }
                        _liveFees.value = updated
                        _lastUpdateMillis.value = System.currentTimeMillis()
                    }
                }
        }

        // Ticker en segundo plano para verificar frescura constante cada 30 segundos
        pollJob?.cancel()
        pollJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(30_000L)
                _lastUpdateMillis.value = System.currentTimeMillis()
            }
        }
    }

    fun getFee(network: UsdtNetwork): Long {
        if (!isInitialized) init()
        return _liveFees.value[network] ?: network.feeEn
    }
}
