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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.delay
import java.text.DateFormat

private data class RequestView(val request: Map<String, Any>? = null, val loading: Boolean = true,
    val unavailable: Boolean = false, val cached: Boolean = false, val target: String = "",
    val serviceCheckedAt: Long? = null, val serviceObservedAt: Long = 0)

@Composable
private fun observeRequest(): RequestView {
    val user = AuthManager.getAuth()?.currentUser
    val uid = user?.uid.takeUnless { AuthManager.isGuestOrUnauthenticated(user) }
    var state by remember(uid) { mutableStateOf(RequestView(loading = uid != null)) }
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
        val listener = uid?.let { FirebaseFirestore.getInstance().collection("economy_requests").document(it)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                state = if (error != null) state.copy(loading = false, unavailable = true)
                    else RequestView(snapshot?.data, false, cached = snapshot?.metadata?.isFromCache == true)
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
    return state.copy(target = targetName, serviceCheckedAt = serviceCheckedAt, serviceObservedAt = serviceObservedAt)
}

@Composable
fun EconomyPendingStatus(alwaysVisible: Boolean = false) {
    val user = AuthManager.getAuth()?.currentUser ?: return
    if (AuthManager.isGuestOrUnauthenticated(user)) return
    val state = observeRequest()
    var details by remember(user.uid) { mutableStateOf(false) }
    val pending = state.request?.get("status") in setOf("PENDING", "PROCESSING", "REVIEW")
    if (alwaysVisible || pending || state.request != null || state.unavailable) {
        CoachOutlinedButton(onClick = { details = true }, modifier = Modifier.fillMaxWidth()
            .padding(bottom = 8.dp).heightIn(min = 48.dp).testTag("economy_request_open")) {
            Text(appTr(if (pending) "Ver solicitud en espera" else "Solicitudes"))
        }
    }
    if (details) EconomyRequestDetails(state.request, state.loading, state.unavailable, state.cached, state.target,
        state.serviceCheckedAt, state.serviceObservedAt) { details = false }
}

@Composable
fun EconomyRequestDialog(onDismiss: () -> Unit) {
    val state = observeRequest()
    EconomyRequestDetails(state.request, state.loading, state.unavailable, state.cached, state.target,
        state.serviceCheckedAt, state.serviceObservedAt, onDismiss)
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
                        "REDEEM" -> "Solicitud de canje USDT"
                        "RESOLVE" -> "Resolución de canje USDT"
                        "SUBSCRIBE" -> "Suscripción a creador"
                        "UNSUBSCRIBE" -> "Cancelar suscripción a creador"
                        "SPONSOR" -> "Envío de anuncio"
                        "CLEANUP" -> "Actualizar historial"
                        else -> "Operación de cuenta"
                    }
                    Text(appTr(action), style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("economy_request_action"))
                    Text(appTr(when (status) {
                        "PENDING" -> "Estado: en espera"
                        "PROCESSING" -> "Estado: en procesamiento"
                        "REVIEW" -> "Estado: requiere revisión"
                        "COMPLETED" -> "Estado: completada"
                        "FAILED" -> "Estado: no completada"
                        else -> "Estado: por confirmar"
                    }), modifier = Modifier.testTag("economy_request_status"))
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
                    if (pending) Text(appTr("La confirmación puede tardar varios minutos o más si el servicio se retrasa. Puedes cerrar este aviso; la solicitud seguirá en espera. No la repitas."))
                    if (pending && serviceStale) Text(appTr("El servicio está retrasado. La solicitud sigue guardada; no la repitas. Consulta Soporte si el retraso continúa."),
                        modifier = Modifier.testTag("economy_service_delayed"))
                    if (status == "REVIEW") Text(appTr("No se pudo confirmar esta operación automáticamente. Contacta con Soporte e indica la fecha de la solicitud. No la repitas."),
                        modifier = Modifier.testTag("economy_request_review"))
                    if (status == "COMPLETED") Text(appTr("La operación fue confirmada. Los datos se actualizarán al sincronizarse."))
                }
            }
            if (cached || unavailable) Text(appTr("Sin confirmación en vivo. El estado mostrado puede estar desactualizado; comprueba tu conexión."))
            Text(appTr("Puedes volver a verla en Usuario → Solicitudes o en Ver solicitud."))
        }
    }, confirmButton = {
        CoachTextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp).testTag("economy_request_close")) {
            Text(appTr("Cerrar"))
        }
    })
}
