package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachIconButton as IconButton

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*
import com.example.util.tr

@Composable
fun DonationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, HextechGold, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = HextechDarkBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HextechGold.copy(alpha = 0.2f))
                                .border(1.dp, HextechGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tr("Apoyar el Proyecto"),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = tr("Donaciones y Comunidad"),
                                color = HextechCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Motivation Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = HextechCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tr("El apoyo a Coach es voluntario y ayuda a mantener el servicio y desarrollar mejoras. No garantiza victorias, rango, actualizaciones continuas ni aprobación de Riot o Google Play."),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.5.sp
                            )
                        }
                    }

                    Text(
                        text = tr("Métodos de Donación Disponibles:"),
                        color = HextechGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Pix (Brasil) - Opciones Combinadas
                    DonationPixCombinedCard()

                    // Option 3: Crypto (USDT - Tron TRC20)
                    DonationCryptoCard(
                        title = "USDT (TRC-20)",
                        network = "TRC20 (Tron)",
                        address = "TPwZJSMizLPVAx67Je7YB2eoK4VUBEh7QH",
                        onCopy = {
                            copyToClipboard(context, "TPwZJSMizLPVAx67Je7YB2eoK4VUBEh7QH", "USDT")
                        }
                    )

                    // Option 4: Bitcoin (BTC)
                    DonationCryptoCard(
                        title = "Bitcoin (BTC)",
                        network = "Red Bitcoin Nativa (BTC)",
                        address = "13fox2wPLWPSmC1AvbYHazXnjU4tSrRETu",
                        onCopy = {
                            copyToClipboard(context, "13fox2wPLWPSmC1AvbYHazXnjU4tSrRETu", "BTC")
                        }
                    )

                    // Thank you note
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = com.example.util.tr(" " + tr("¡Gracias por apoyar a la comunidad!")),
                                color = HextechGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr("Cada aporte cuenta para seguir mejorando el asistente táctico, las builds y la precisión de análisis en tiempo real."),
                                color = TextMuted,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HextechGold,
                        contentColor = HextechDarkBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(tr("Entendido / Cerrar"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun DonationMethodCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(com.example.util.tr(title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(com.example.util.tr(subtitle), color = TextMuted, fontSize = 11.sp)
                }
            }

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(com.example.util.tr(actionText), color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DonationCryptoCard(
    title: String,
    network: String,
    address: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(com.example.util.tr(title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Text(com.example.util.tr(network), color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                }
                OutlinedButton(
                    onClick = onCopy,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechGold),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = com.example.util.trNullable("Copiar"), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Copiar"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechDarkBg)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .coachClickable { onCopy() }
            ) {
                Text(
                    text = com.example.util.tr(address),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DonationPixCard(
    title: String,
    amountText: String,
    pixCode: String,
    onCopy: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showQRModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val qrCodeUrl = androidx.compose.runtime.remember(pixCode) {
        val encoded = android.net.Uri.encode(pixCode)
        "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=$encoded&bgcolor=ffffff&color=000000&margin=2"
    }

    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.5f))
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.QrCode,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFF32BCAD),
                    modifier = androidx.compose.ui.Modifier.size(20.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                androidx.compose.material3.Text(
                    text = com.example.util.tr(title),
                    color = TextPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 15.sp
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = tr("INSTANTÁNEO"),
                        color = androidx.compose.ui.graphics.Color(0xFF32BCAD),
                        fontSize = 9.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
            androidx.compose.material3.Text(
                text = tr("QR Code e Pix Copia e Cola"),
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

            // QR code centrado y más grande
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .size(160.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color.White)
                    .coachClickable { showQRModal = true }
                    .border(2.dp, androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(qrCodeUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                        .build(),
                    contentDescription = "QR Pix",
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

            com.example.ui.components.CoachButton(
                onClick = { showQRModal = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF32BCAD)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.QrCode2, contentDescription = com.example.util.trNullable("Ver QR"), modifier = androidx.compose.ui.Modifier.size(18.dp), tint = androidx.compose.ui.graphics.Color.Black)
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                androidx.compose.material3.Text(tr("Ver QR Ampliado"), color = androidx.compose.ui.graphics.Color.Black, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }

    if (showQRModal) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showQRModal = false }) {
            androidx.compose.material3.Card(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth(0.95f)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .border(1.5.dp, androidx.compose.ui.graphics.Color(0xFF32BCAD), androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = HextechDarkBg)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.QrCode2, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF32BCAD))
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                            androidx.compose.material3.Text(
                                text = com.example.util.tr("Pix QR Code"),
                                color = TextPrimary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        com.example.ui.components.CoachIconButton(onClick = { showQRModal = false }, modifier = androidx.compose.ui.Modifier.size(28.dp)) {
                            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                        }
                    }

                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .size(260.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(androidx.compose.ui.graphics.Color.White)
                            .border(2.dp, androidx.compose.ui.graphics.Color(0xFF32BCAD), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(qrCodeUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                                .build(),
                            contentDescription = com.example.util.tr("Pix QR Ampliado"),
                            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }

                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

                    androidx.compose.material3.Text(
                        text = com.example.util.tr("Beneficiario: BRLA DIGITAL LTDA"),
                        color = HextechGold,
                        fontSize = 13.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    androidx.compose.material3.Text(
                        text = com.example.util.tr(amountText),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))

                    com.example.ui.components.CoachButton(
                        onClick = {
                            onCopy()
                            showQRModal = false
                        },
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF32BCAD)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    ) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.ContentCopy, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Black, modifier = androidx.compose.ui.Modifier.size(16.dp))
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        androidx.compose.material3.Text(tr("Copiar Código Pix"), color = androidx.compose.ui.graphics.Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                    }

                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))

                    com.example.ui.components.CoachOutlinedButton(
                        onClick = { downloadQr(context, qrCodeUrl) },
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFF32BCAD)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.6f)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    ) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Download, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF32BCAD), modifier = androidx.compose.ui.Modifier.size(16.dp))
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        androidx.compose.material3.Text(tr("Descargar QR"), color = androidx.compose.ui.graphics.Color(0xFF32BCAD), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        copyToClipboard(context, url, "Enlace")
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, com.example.util.appTr("Copiado al portapapeles: $label"), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        // ignore
    }
}

private fun downloadQr(context: Context, url: String) {
    try {
        val request = android.app.DownloadManager.Request(Uri.parse(url))
        request.setTitle(com.example.util.appTr("Pix QR Code"))
        request.setDescription(com.example.util.appTr("Descargando código QR Pix"))
        request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "Pix_QR.png")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, com.example.util.appTr("Descarga iniciada..."), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, com.example.util.appTr("Error al descargar"), Toast.LENGTH_SHORT).show()
    }
}


@Composable
private fun DonationPixCombinedCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedOption by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Int?>(null) }
    var showQRModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val pixCode1 = "00020126580014br.gov.bcb.pix0136ff439919-4119-405d-838a-6c3e3efd8b5552040000530398654045.275802BR5917BRLA DIGITAL LTDA6009Sao Paulo62290525c898e88196a346fa968d9eada6304654C"
    val pixCode2 = "00020126580014br.gov.bcb.pix0136ff439919-4119-405d-838a-6c3e3efd8b55520400005303986540526.455802BR5917BRLA DIGITAL LTDA6009Sao Paulo622905258e3dc64ffc0c48fab562857a5630478FA"

    val currentPixCode = if (selectedOption == 1) pixCode1 else if (selectedOption == 2) pixCode2 else ""
    val currentAmountText = if (selectedOption == 1) "R$ 5.27" else if (selectedOption == 2) "R$ 26.45" else ""

    val qrCodeUrl = androidx.compose.runtime.remember(currentPixCode) {
        val encoded = android.net.Uri.encode(currentPixCode)
        "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=$encoded&bgcolor=ffffff&color=000000&margin=2"
    }

    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.5f))
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.QrCode,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFF32BCAD),
                    modifier = androidx.compose.ui.Modifier.size(20.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                androidx.compose.material3.Text(
                    text = com.example.util.tr("Pix (Brasil)"),
                    color = TextPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 15.sp
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = tr("INSTANTÁNEO"),
                        color = androidx.compose.ui.graphics.Color(0xFF32BCAD),
                        fontSize = 9.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))

            // Selector de opciones
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                val isOption1 = selectedOption == 1
                com.example.ui.components.CoachOutlinedButton(
                    onClick = { selectedOption = 1 },
                    modifier = androidx.compose.ui.Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isOption1) androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = if (isOption1) androidx.compose.ui.graphics.Color(0xFF32BCAD) else TextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isOption1) androidx.compose.ui.graphics.Color(0xFF32BCAD) else TextSecondary.copy(alpha = 0.5f))
                ) {
                    androidx.compose.material3.Text(tr("Opción 1"), fontSize = 12.sp, maxLines = 1)
                }

                val isOption2 = selectedOption == 2
                com.example.ui.components.CoachOutlinedButton(
                    onClick = { selectedOption = 2 },
                    modifier = androidx.compose.ui.Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isOption2) androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = if (isOption2) androidx.compose.ui.graphics.Color(0xFF32BCAD) else TextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isOption2) androidx.compose.ui.graphics.Color(0xFF32BCAD) else TextSecondary.copy(alpha = 0.5f))
                ) {
                    androidx.compose.material3.Text(tr("Opción 2"), fontSize = 12.sp, maxLines = 1)
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))

            if (selectedOption != null) {
                androidx.compose.material3.Text(
                    text = com.example.util.tr("Sao Paulo • $currentAmountText"),
                    color = HextechGold,
                    fontSize = 13.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )

                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                androidx.compose.material3.Text(
                    text = tr("QR Code e Pix Copia e Cola"),
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

                // QR code centrado
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(160.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color.White)
                        .coachClickable { showQRModal = true }
                        .border(2.dp, androidx.compose.ui.graphics.Color(0xFF32BCAD).copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(context)
                            .data(qrCodeUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                            .build(),
                        contentDescription = "QR Pix",
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

                com.example.ui.components.CoachButton(
                    onClick = { copyToClipboard(context, currentPixCode, "Código Pix Copia e Cola") },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = HextechBlue),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(0.8f)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                        contentDescription = com.example.util.trNullable("Copy"),
                        modifier = androidx.compose.ui.Modifier.size(16.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                    androidx.compose.material3.Text(tr("Copiar Código Pix"), fontSize = 13.sp)
                }
            }
        }
    }

    if (showQRModal) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQRModal = false },
            containerColor = HextechSurface,
            title = {
                androidx.compose.material3.Text(
                    text = tr("Escanea el Código QR"),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            },
            text = {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .size(240.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(androidx.compose.ui.graphics.Color.White)
                            .padding(12.dp)
                    ) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(qrCodeUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                                .build(),
                            contentDescription = "QR Pix",
                            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    androidx.compose.material3.Text(
                        text = com.example.util.tr("${tr("Valor:")} $currentAmountText"),
                        color = HextechGold,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                    androidx.compose.material3.Text(
                        text = tr("Usa la opción 'Pix Copia e Cola' o escanea el QR en tu app de banco."),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                com.example.ui.components.CoachTextButton(onClick = { showQRModal = false }) {
                    androidx.compose.material3.Text(tr("Cerrar"), color = HextechGold)
                }
            }
        )
    }
}
