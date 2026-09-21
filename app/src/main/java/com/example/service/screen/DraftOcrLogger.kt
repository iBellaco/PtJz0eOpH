package com.example.service.screen

import android.util.Log
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.util.AppLogger

/**
 * Mecanismo de logging y diagnóstico en consola para el escáner de Draft.
 * 
 * Emite a la consola estándar (System.out), Logcat de Android ("DraftOCR") y al visor
 * de depuración en tiempo real del Asistente (AppLogger).
 * 
 * Traza con precisión quirúrgica:
 * 1. Textos OCR brutos detectados por slot.
 * 2. Fase de eliminación y desprendimiento de iconos de maestría y glifos.
 * 3. Reemplazo y transición de cadenas desde nombre de carril asignado a nombre del campeón.
 * 4. Diagnóstico detallado del motivo exacto cuando la detección no tiene éxito.
 */
object DraftOcrLogger {
    private const val TAG = "DraftOCR"

    private fun output(message: String) {
        val formatted = "[DraftOCR] $message"
        println(formatted)
        Log.i(TAG, message)
        AppLogger.i(TAG, message)
    }

    /**
     * Cabecera del ciclo de escaneo con estado de turnos y prioridades.
     */
    fun logCycleHeader(
        cycle: Long,
        confirmedCount: Int,
        activeTurns: List<DraftPickTurn>,
        isFirstPick: Boolean?
    ) {
        output("================================================================================")
        output("CICLO DE ESCANEO #$cycle | Picks Confirmados: $confirmedCount/10 | 1ª Selección: ${isFirstPick ?: "Indeterminada"}")
        if (activeTurns.isNotEmpty()) {
            val turnsStr = activeTurns.joinToString(", ") { 
                "${if (it.isAlly) "Aliado" else "Rival"} Slot ${it.slotIndex} (Turno ${it.turnNumber})" 
            }
            output("SELECCIÓN ACTIVA [ALTA PRIORIDAD]: $turnsStr")
        } else {
            output("SELECCIÓN ACTIVA: Ningún turno pendiente (Draft completo o en preparación)")
        }
        output("================================================================================")
    }

    /**
     * Registro de textos brutos capturados en un slot específico.
     */
    fun logSlotRawText(
        slotIndex: Int,
        isAlly: Boolean,
        isCurrentSelection: Boolean,
        rawEntries: List<String>
    ) {
        val team = if (isAlly) "Aliado" else "Rival"
        val priorityTag = if (isCurrentSelection) "[PRIORIDAD ALTA - TURNO ACTIVO]" else "[Frecuencia Reducida]"
        val entriesStr = if (rawEntries.isEmpty()) {
            "<Sin texto detectado>"
        } else {
            rawEntries.joinToString(" | ") { "'$it'" }
        }
        output("[$team Slot $slotIndex] $priorityTag Textos brutos: $entriesStr")
    }

    /**
     * Traza detallada de la fase de eliminación de iconos de maestría y símbolos.
     */
    fun logMasteryRemoval(
        slotIndex: Int,
        isAlly: Boolean,
        audit: DraftValidationLayer.MasteryRemovalAudit
    ) {
        val team = if (isAlly) "Aliado" else "Rival"
        if (audit.stepsApplied.isEmpty()) {
            output("  [$team Slot $slotIndex][Anti-Maestría] Texto sin iconos que remover: '${audit.rawText}'")
        } else {
            output("  [$team Slot $slotIndex][Anti-Maestría] Texto original: '${audit.rawText}'")
            audit.stepsApplied.forEach { step ->
                output("    -> Regla: $step")
            }
            output("    -> Resultado limpio: '${audit.cleanText}'")
        }
    }

    /**
     * Traza de la transición entre la cadena de la línea/carril y el nombre del campeón.
     */
    fun logTransitionSuccess(
        slotIndex: Int,
        previousState: String,
        rawText: String,
        strippedText: String,
        champion: Champion,
        viaPreprocessing: Boolean = false
    ) {
        val method = if (viaPreprocessing) "Pre-procesamiento Adaptativo" else "OCR Global"
        output("  [Aliado Slot $slotIndex][TRANSICIÓN EXITOSA][$method]")
        output("    -> Estado previo del slot: $previousState")
        output("    -> Cadena original: '$rawText' => Cadena sin maestría: '$strippedText'")
        output("    -> Reemplazo ejecutado: Se confirmó campeón '${champion.name}' (ID: ${champion.id}, Rol primario: ${champion.primaryRole.shortName})")
    }

    /**
     * Traza cuando el slot aún no ha seleccionado y permanece mostrando el nombre de la línea asignada.
     */
    fun logTransitionWaiting(
        slotIndex: Int,
        previousState: String,
        rawText: String,
        strippedText: String,
        role: LaneRole
    ) {
        output("  [Aliado Slot $slotIndex][EN ESPERA DE SELECCIÓN]")
        output("    -> Estado actual del slot: $previousState")
        output("    -> Cadena OCR: '$rawText' => Sin maestría: '$strippedText'")
        output("    -> El jugador aún no fija campeón; el slot continúa mostrando la línea asignada '${role.shortName}'")
    }

    /**
     * Traza explícita cuando un texto en el slot falla en ser reconocido como campeón o carril,
     * identificando la causa exacta de por qué la detección está fallando.
     */
    fun logDetectionFailure(
        slotIndex: Int,
        isAlly: Boolean,
        previousState: String,
        rawText: String,
        strippedText: String,
        closestCandidates: List<Pair<String, Int>>
    ) {
        val team = if (isAlly) "Aliado" else "Rival"
        output("  [$team Slot $slotIndex][FALLO DE DETECCIÓN]")
        output("    -> Estado previo del slot: $previousState")
        output("    -> Cadena OCR bruta: '$rawText'")
        output("    -> Cadena tras anti-maestría: '$strippedText'")
        output("    -> Causa del fallo: La cadena no coincide con ningún carril conocido (Top, Jg, Mid, Duo, Sup)")
        output("       ni con ningún nombre canónico de campeón de Wild Rift.")
        if (closestCandidates.isNotEmpty()) {
            val candidatesStr = closestCandidates.joinToString(", ") { (cand, dist) ->
                "'$cand' (Distancia: $dist)"
            }
            output("    -> Campeones más cercanos fonética/visualmente: $candidatesStr")
        }
        output("    -> Posible causa: Nombre de invocador, artefacto de compresión de video, o texto parcialmente ocluido.")
    }

    /**
     * Traza para el slot enemigo cuando se confirma o se detecta estado de espera.
     */
    fun logEnemySlotEvaluation(
        slotIndex: Int,
        rawText: String,
        strippedText: String,
        champion: Champion?,
        isPlayerWaiting: Boolean
    ) {
        if (champion != null) {
            output("  [Rival Slot $slotIndex][CONFIRMADO] Campeón rival detectado: '${champion.name}' (Entrada: '$rawText' => Limpio: '$strippedText')")
        } else if (isPlayerWaiting) {
            output("  [Rival Slot $slotIndex][EN ESPERA] Rival en espera ('$rawText')")
        }
    }
}
