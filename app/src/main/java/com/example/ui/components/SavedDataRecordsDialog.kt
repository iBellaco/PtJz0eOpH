package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.data.SavedDataDeletion
import com.example.util.tr
import kotlinx.coroutines.launch

@Composable
fun SavedDataRecordsDialog(category: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var records by remember { mutableStateOf(emptyList<SavedDataDeletion.Record>()) }
    var selected by remember { mutableStateOf<SavedDataDeletion.Record?>(null) }
    var busy by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(category) {
        try { records = SavedDataDeletion.records(category) }
        catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
        finally { busy = false }
    }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(tr(category)) }, text = {
        Column {
            if (busy) CircularProgressIndicator()
            if (failed) Text(tr("No se pudieron consultar los datos. Vuelve a intentarlo."))
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(records, key = { it.path }) { record ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(record.label, Modifier.weight(1f))
                        IconButton(enabled = !busy, onClick = { selected = record }, modifier = Modifier.size(48.dp).testTag("delete_saved_record")) {
                            Icon(Icons.Default.Delete, contentDescription = tr("Eliminar"))
                        }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text(tr("Cerrar")) } })
    selected?.let { record ->
        AlertDialog(onDismissRequest = { if (!busy) selected = null },
            title = { Text(stringResource(com.example.R.string.delete_data_confirm)) },
            text = { Text(record.label) }, confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    busy = true
                    scope.launch {
                        try {
                            SavedDataDeletion.delete(context, category, record)
                            records = records.filterNot { it.path == record.path }
                            selected = null
                        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                        catch (_: Exception) { failed = true; selected = null }
                        finally { busy = false }
                    }
                }) { Text(tr("Eliminar")) }
            }, dismissButton = { TextButton(enabled = !busy, onClick = { selected = null }) { Text(tr("Cancelar")) } })
    }
}
