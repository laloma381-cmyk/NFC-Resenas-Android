package com.nfcresenas.app.ui.screens

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.nfcresenas.app.R
import com.nfcresenas.app.data.Business
import com.nfcresenas.app.ui.QRCodeGenerator
import java.io.File

private val BrandBlue = Color(0xFF0A5EDB)
private val Ink = Color(0xFF10243A)
private val Muted = Color(0xFF66758A)
private val Page = Color(0xFFF3F7FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    business: Business,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(business.googleReviewUrl) {
        business.googleReviewUrl.takeIf(String::isNotBlank)?.let(QRCodeGenerator::generateQrBitmap)
    }
    val reviewMessage = remember(business.name) {
        QRCodeGenerator.buildReviewMessage(business.name)
    }
    val shareText = remember(business, reviewMessage) {
        buildString {
            appendLine(business.name)
            if (business.category.isNotBlank()) appendLine("Categoría: ${business.category}")
            if (business.address.isNotBlank()) appendLine("Dirección: ${business.address}")
            if (business.phone.isNotBlank()) appendLine("Teléfono: ${business.phone}")
            if (business.email.isNotBlank()) appendLine("Correo: ${business.email}")
            if (business.googleReviewUrl.isNotBlank()) appendLine("Deja tu reseña: ${business.googleReviewUrl}")
            appendLine()
            append(reviewMessage)
        }
    }

    Scaffold(
        containerColor = Page,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        business.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } },
                actions = { TextButton(onClick = onEdit) { Text("Editar") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Page)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BrandBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_icon),
                        contentDescription = "Logo de NFC Reseñas",
                        modifier = Modifier.size(52.dp)
                    )
                    Text(
                        "¡NOS ENCANTARÍA CONOCER TU OPINIÓN!",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Código QR para dejar una reseña de ${business.name}",
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .aspectRatio(1f)
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .padding(12.dp)
                        )
                    } else {
                        Text(
                            "Añade un enlace válido para generar el código QR.",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(business.name, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Escanea el código para dejar una reseña", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "${business.name} — reseñas")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir negocio"))
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Compartir datos", fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick = {
                        val bitmap = qrBitmap
                        if (bitmap == null) {
                            Toast.makeText(context, "No hay un QR para compartir", Toast.LENGTH_SHORT).show()
                        } else {
                            try {
                                val directory = File(context.cacheDir, "shared_qr").apply { mkdirs() }
                                val imageFile = File(directory, "qr-resenas-${business.id}.png")
                                imageFile.outputStream().use { output ->
                                    check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output))
                                }
                                val imageUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    imageFile
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, imageUri)
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    clipData = ClipData.newRawUri("Código QR de ${business.name}", imageUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir código QR"))
                            } catch (_: Exception) {
                                Toast.makeText(context, "No se pudo preparar el QR para compartir", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = qrBitmap != null,
                    modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Compartir QR", fontWeight = FontWeight.SemiBold) }
            }

            InfoCard(title = "Información del negocio") {
                InfoRow("Categoría", business.category.ifBlank { "Sin categoría" })
                InfoRow("Dirección", business.address.ifBlank { "Sin dirección" })
                InfoRow("Teléfono", business.phone.ifBlank { "Sin teléfono" })
                InfoRow("Correo", business.email.ifBlank { "Sin correo" })
            }

            InfoCard(title = "Enlace de reseñas") {
                Text(
                    business.googleReviewUrl.ifBlank { "Sin URL" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
                HorizontalDivider(color = Color(0xFFE7ECF3))
                Text("Mensaje sugerido", style = MaterialTheme.typography.labelLarge, color = Muted)
                Text(reviewMessage, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }

            Button(
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(business.googleReviewUrl)))
                    } catch (_: Exception) {
                        Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = business.googleReviewUrl.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Abrir página de reseñas", fontWeight = FontWeight.Bold) }

            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Eliminar negocio") }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink)
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Muted)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Ink)
    }
}
