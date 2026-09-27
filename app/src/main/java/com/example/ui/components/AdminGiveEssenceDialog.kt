package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.util.SubscriptionHistoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

@Composable
fun AdminGiveEssenceDialog(
    userUid: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("BLUE") } // "BLUE" or "ORANGE"
    var isAddition by remember { mutableStateOf(true) } // true: Añadir (+), false: Descontar (-)
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, if (selectedCurrency == "BLUE") Color(0xFF0EA5E9) else Color(0xFFFF8C00))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = if (selectedCurrency == "BLUE") R.drawable.ic_blue_essence else R.drawable.ic_orange_essence),
                        contentDescription = "Esencia",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ajustar Esencias de Usuario",
                        color = if (selectedCurrency == "BLUE") Color(0xFF0EA5E9) else Color(0xFFFF9E1B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Currency selector (Blue / Orange)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedCurrency = "BLUE" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCurrency == "BLUE") Color(0xFF0EA5E9) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_blue_essence),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Azul (EA)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedCurrency = "ORANGE" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCurrency == "ORANGE") Color(0xFFFF8C00) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_orange_essence),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Naranja (EN)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Operation toggle: Añadir (+) vs Descontar (-)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isAddition = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isAddition) Color(0xFF00FF7F).copy(alpha = 0.2f) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (isAddition) Color(0xFF00FF7F) else Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Añadir", color = if (isAddition) Color(0xFF00FF7F) else Color.Gray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { isAddition = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isAddition) Color(0xFFFF5252).copy(alpha = 0.2f) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (!isAddition) Color(0xFFFF5252) else Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("- Descontar", color = if (!isAddition) Color(0xFFFF5252) else Color.Gray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Cantidad", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = if (selectedCurrency == "BLUE") Color(0xFF0EA5E9) else Color(0xFFFF8C00)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isProcessing) {
                        Text("Cancelar", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsed = amount.toLongOrNull()
                            if (parsed != null && parsed > 0) {
                                isProcessing = true
                                val effectiveDelta = if (isAddition) parsed else -parsed
                                val fieldName = if (selectedCurrency == "BLUE") "blueEssence" else "orangeEssence"
                                val currTag = if (selectedCurrency == "BLUE") "EA" else "EN"
                                val status = if (isAddition) "Añadido por Administrador" else "Descontado por Administrador"
                                val amountStr = "${if (isAddition) "+" else "-"}$parsed $currTag"

                                FirebaseFirestore.getInstance().collection("users").document(userUid)
                                    .update(fieldName, FieldValue.increment(effectiveDelta))
                                    .addOnSuccessListener {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            SubscriptionHistoryManager.addRecordForUser(
                                                uid = userUid,
                                                durationMillis = 0L,
                                                planName = "Ajuste de Administrador",
                                                status = status,
                                                amount = amountStr
                                            )
                                        }
                                        isProcessing = false
                                        onSuccess()
                                        onDismiss()
                                    }
                                    .addOnFailureListener {
                                        isProcessing = false
                                    }
                            }
                        },
                        enabled = !isProcessing && amount.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCurrency == "BLUE") Color(0xFF0EA5E9) else Color(0xFFFF8C00)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text(if (isAddition) "Añadir Esencia" else "Descontar Esencia", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
