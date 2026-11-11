package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppUserRole
import com.example.util.AuthManager
import com.example.util.AppLanguage
import com.example.util.appTr
import com.example.data.authoritativePendingRequests
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.delay
import java.text.DateFormat

private data class RequestView(val request: Map<String, Any>? = null, val loading: Boolean = true,
    val unavailable: Boolean = false, val cached: Boolean = false, val target: String = "",
    val serviceCheckedAt: Long? = null, val serviceObservedAt: Long = 0,
    val requests: List<Map<String, Any>> = emptyList())

/** Match current and legacy identifiers for a sponsor command. */
internal fun isSettledSponsorRequest(row: Map<String, Any>, settledIds: Set<String>): Boolean {
    val payload = row["payload"] as? Map<*, *> ?: return false
    if (payload["action"]?.toString() != "SPONSOR") return false
    val ids = mutableSetOf<String>()
    listOf("id", "sponsorId", "queuedRequestId").forEach { key ->
        payload[key]?.toString()?.takeIf { it.isNotBlank() }?.let(ids::add)
    }
    row["operationId"]?.toString()?.takeIf { it.isNotBlank() }?.let(ids::add)
    (payload["notice"] as? Map<*, *>)?.get("id")?.toString()
        ?.takeIf { it.isNotBlank() }?.let(ids::add)
    return ids.any { it in settledIds }
}

@Composable
private fun observeRequest(): RequestView {
    val user = AuthManager.getAuth()?.currentUser
    val uid = user?.uid.takeUnless { AuthManager.isGuestOrUnauthenticated(user) }
    var state by remember(uid) { mutableStateOf(RequestView(loading = uid != null)) }
    var resolvedSponsorIds by remember(uid) { mutableStateOf<Set<String>>(emptySet()) }
    var serviceCheckedAt by remember(uid) { mutableStateOf<Long?>(null) }
    var serviceObservedAt by remember(uid) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(uid) {
        while (uid != null) { delay(60_000L); serviceObservedAt = System.currentTimeMillis() }
    }
    DisposableEffect(uid) {
        val listener = uid?.let { FirebaseFirestore.getInstance().collection("system_config").document("economy_service")
            .addSnapshotListener { snapshot, error ->
                serviceCheckedAt = if (error != null || snapshot?.getBoolean("enabled") != true) 0L
                    else snapshot.getTimestamp("checkedAt")?.toDate()?.time ?: 0L
            } }
        onDispose { listener?.remove() }
    }
    DisposableEffect(uid) {
        val listener = uid?.let { ownerUid ->
            FirebaseFirestore.getInstance().collection("pending_sponsor_ads")
                .whereEqualTo("userId", ownerUid)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                    if (error == null && snapshot?.metadata?.isFromCache != true) {
                        resolvedSponsorIds = snapshot?.documents.orEmpty().filter {
                            it.getBoolean("isApproved") == true ||
                                it.getString("status")?.uppercase() in setOf("REJECTED", "REFUNDED")
                        }.flatMapTo(mutableSetOf()) { document ->
                            listOf(document.id, document.getString("id"),
                                document.getString("queuedRequestId"), document.getString("operationId"))
                                .filterNotNull().filter(String::isNotBlank)
                        }
                    }
                }
        }
        onDispose { listener?.remove() }
    }
    DisposableEffect(uid) {
        val listener = uid?.let { FirebaseFirestore.getInstance().collection("economy_requests").whereEqualTo("userId", it)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) state = state.copy(loading = false, unavailable = true)
                else {
                    val rows = snapshot?.documents.orEmpty().mapNotNull { it.data }
                    val requests = authoritativePendingRequests(
                        fromCache = snapshot?.metadata?.isFromCache == true,
                        hasError = false,
                        rows = rows
                    ).filterNot { row -> isSettledSponsorRequest(row, resolvedSponsorIds) }
                        .sortedWith(compareByDescending<Map<String, Any>> { it["status"] in setOf("PENDING", "PROCESSING", "REVIEW") }
                            .thenByDescending { (it["createdAt"] as? Timestamp)?.toDate()?.time ?: 0L })
                    state = RequestView(requests.firstOrNull(), false,
                        cached = snapshot?.metadata?.isFromCache == true, requests = requests)
                }
            } }
        onDispose { listener?.remove() }
    }
    val payload = state.request?.get("payload") as? Map<*, *>
    val targetUid = (payload?.get("uid") ?: payload?.get("creatorUid")) as? String
    var targetName by remember(uid, targetUid) { mutableStateOf("") }
    // Profile lookup is optional; never keep the dialog waiting for another account's permissions.
    DisposableEffect(uid, targetUid) {
        val listener = if (uid != null && !targetUid.isNullOrBlank()) FirebaseFirestore.getInstance().collection("users").document(targetUid)
            .addSnapshotListener { snapshot, error ->
                if (error == null) targetName = snapshot?.getString("name").orEmpty()
            } else null
        onDispose { listener?.remove() }
    }
    val visibleRequests = state.requests.filterNot { row -> isSettledSponsorRequest(row, resolvedSponsorIds) }
    return state.copy(
        request = visibleRequests.firstOrNull(), requests = visibleRequests,
        target = targetName, serviceCheckedAt = serviceCheckedAt, serviceObservedAt = serviceObservedAt
    )
}

@Composable
fun EconomyPendingStatus(alwaysVisible: Boolean = false) {
    val user = AuthManager.getAuth()?.currentUser ?: return
    if (AuthManager.isGuestOrUnauthenticated(user)) return
    val state = observeRequest()
    var details by remember(user.uid) { mutableStateOf(false) }
    val pending = !state.cached && !state.unavailable &&
        state.request?.get("status") in setOf("PENDING", "PROCESSING", "REVIEW")
    if (alwaysVisible || pending || state.request != null || state.unavailable) {
        CoachOutlinedButton(onClick = { details = true }, modifier = Modifier.fillMaxWidth()
            .padding(bottom = 8.dp).heightIn(min = 48.dp).testTag("economy_request_open")) {
            Text(appTr(if (pending) "Ver solicitud en espera" else "Solicitudes"))
        }
    }
    if (details) EconomyRequestDialog { details = false }
}

@Composable
fun EconomyPendingInboxCount(): Int {
    val user = AuthManager.getAuth()?.currentUser ?: return 0
    if (AuthManager.isGuestOrUnauthenticated(user)) return 0
    val state = observeRequest()
    return state.requests.count {
        it["status"] in setOf("PENDING", "PROCESSING", "REVIEW") &&
            (it["payload"] as? Map<*, *>)?.get("action") != "SPONSOR"
    }
}

@Composable
fun EconomyRequestDialog(onDismiss: () -> Unit) {
    val state = observeRequest()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = state.requests.firstOrNull { it["operationId"] == selectedId }
    if (selected != null) {
        EconomyRequestDetails(selected, state.loading, state.unavailable, state.cached,
            serviceCheckedAt = state.serviceCheckedAt, serviceObservedAt = state.serviceObservedAt) { selectedId = null }
    } else AlertDialog(onDismissRequest = onDismiss, title = { Text(appTr("Solicitudes")) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            if (state.loading) Text(appTr("Consultando solicitud…"))
            if (state.unavailable || state.cached) Text(appTr("Sin confirmación en vivo. El estado mostrado puede estar desactualizado; comprueba tu conexión."))
            if (!state.loading && !state.unavailable && state.requests.isEmpty()) Text(appTr("No tienes solicitudes pendientes."))
            state.requests.forEach { request ->
                CoachOutlinedButton(onClick = { selectedId = request["operationId"] as? String },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("economy_request_${request["operationId"]}")) {
                    val payload = request["payload"] as? Map<*, *>
                    val notice = payload?.get("notice") as? Map<*, *>
                    Column {
                        Text(notice?.get("title") as? String ?: appTr(if (payload?.get("action") == "ADJUST") "Ajuste de esencias" else "Operación de cuenta"))
                        Text(appTr(requestStatusText(request["status"] as? String)))
                    }
                }
            }
        }
    }, confirmButton = { CoachTextButton(onClick = onDismiss,
        modifier = Modifier.heightIn(min = 48.dp).testTag("economy_requests_close")) { Text(appTr("Cerrar")) } })
}

private fun requestStatusText(status: String?): String = when (status) {
    "PENDING" -> "Estado: en espera"
    "PROCESSING" -> "Estado: en procesamiento"
    "REVIEW" -> "Estado: requiere revisión"
    "COMPLETED" -> "Estado: completada"
    "FAILED" -> "Estado: no completada"
    else -> "Estado: por confirmar"
}

/** Read-only details. Closing never cancels, repeats, or confirms a command. */
@Composable
fun EconomyRequestDetails(request: Map<String, Any>?, loading: Boolean = false, unavailable: Boolean = false,
    cached: Boolean = false, targetName: String = "", serviceCheckedAt: Long? = null,
    serviceObservedAt: Long = 0, onDismiss: () -> Unit) {
    val payload = request?.get("payload") as? Map<*, *> ?: emptyMap<Any, Any>()
    val status = request?.get("status") as? String
    val pending = status in setOf("PENDING", "PROCESSING")
    val serviceStale = serviceCheckedAt != null && serviceObservedAt - serviceCheckedAt > 15 * 60_000L
    AlertDialog(onDismissRequest = onDismiss, title = { Text(appTr("Tu solicitud")) }, text = {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag("economy_request_details"),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                loading -> Text(appTr("Consultando solicitud…"))
                request == null -> Text(appTr(if (unavailable) "No se pudo consultar la solicitud. Comprueba tu conexión y vuelve a abrir este aviso."
                    else "No tienes solicitudes pendientes."))
                else -> {
                    val action = when (payload["action"]) {
                        "ROLE" -> "Cambio de rol"
                        "PREMIUM_GRANT" -> "Asignar tiempo Premium"
                        "PREMIUM_REMOVE" -> "Retirar tiempo Premium"
                        "PURCHASE" -> "Compra de Premium"
                        "ADJUST" -> "Ajuste de esencias"
                        "REDEEM", "RESOLVE" -> "Historial"
                        "SUBSCRIBE" -> "Suscripción a creador"
                        "UNSUBSCRIBE" -> "Cancelar suscripción a creador"
                        "SPONSOR" -> "Envío de anuncio"
                        "CLEANUP" -> "Actualizar historial"
                        else -> "Operación de cuenta"
                    }
                    Text(appTr(action), style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("economy_request_action"))
                    Text(appTr(requestStatusText(status)), modifier = Modifier.testTag("economy_request_status"))
                    if (targetName.isNotBlank()) Text(appTr("Cuenta") + ": " + targetName, modifier = Modifier.testTag("economy_request_target"))
                    else if (payload["uid"] != null || payload["creatorUid"] != null) Text(appTr("Destino: cuenta seleccionada"))
                    if (payload["action"] == "ROLE") Text(appTr("Rol solicitado") + ": " + appTr(AppUserRole.fromId(payload["role"] as? String ?: "free").displayName))
                    (payload["amount"] as? Number)?.let { Text(appTr("Cantidad") + ": " + it.toString()) }
                    (payload["days"] as? Number)?.let { Text(appTr("Días") + ": " + it.toString()) }
                    (payload["currency"] as? String)?.let { Text(appTr(if (it == "BLUE") "Esencia Azul" else "Esencia Naranja")) }
                    (request["createdAt"] as? Timestamp)?.let {
                        Text(appTr("Enviada") + ": " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
                            AppLanguage.locale(AppLanguage.current.value)).format(it.toDate()))
                    }
                    if (status == "FAILED") {
                        val error = request["error"] as? Map<*, *>
                        Text(appTr(error?.get("message") as? String ?: "No se pudo completar la operación. Vuelve a intentarlo."))
                    }
                    if (pending) Text(appTr("Esta solicitud se procesa automáticamente; no tienes que aceptarla. Puede tardar varios minutos. No la repitas."))
                    if (pending && serviceStale) Text(appTr("El servicio está retrasado. La solicitud sigue guardada; no la repitas. Consulta Soporte si el retraso continúa."),
                        modifier = Modifier.testTag("economy_service_delayed"))
                    if (status == "REVIEW") Text(appTr("No se pudo confirmar esta operación automáticamente. Contacta con Soporte e indica la fecha de la solicitud. No la repitas."),
                        modifier = Modifier.testTag("economy_request_review"))
                    if (status == "COMPLETED") Text(appTr("La operación fue confirmada. Los datos se actualizarán al sincronizarse."))
                }
            }
            if (cached || unavailable) Text(appTr("Sin confirmación en vivo. El estado mostrado puede estar desactualizado; comprueba tu conexión."))
        }
    }, confirmButton = {
        CoachTextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp).testTag("economy_request_close")) {
            Text(appTr("Cerrar"))
        }
    })
}
