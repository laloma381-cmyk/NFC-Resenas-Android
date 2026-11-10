package com.nfcresenas.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcresenas.app.data.Business
import com.nfcresenas.app.ui.QRCodeGenerator

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
        QRCodeGenerator.generateQrBitmap(business.googleReviewUrl)
    }
    val reviewMessage = remember(business.name) {
        QRCodeGenerator.buildReviewMessage(business.name)
    }

    val shareText = buildString {
        appendLine(business.name)
        if (business.category.isNotBlank()) appendLine("Categoría: ${business.category}")
        if (business.address.isNotBlank()) appendLine("Dirección: ${business.address}")
        if (business.phone.isNotBlank()) appendLine("Teléfono: ${business.phone}")
        if (business.email.isNotBlank()) appendLine("Correo: ${business.email}")
        if (business.googleReviewUrl.isNotBlank()) {
            appendLine("Enlace de reseñas: ${business.googleReviewUrl}")
        }
        appendLine()
        append(reviewMessage)
    }

    Scaffold(
        containerColor = Color(0xFFF4F7FC),
        topBar = {
            TopAppBar(
                title = { Text(business.name, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                },
                actions = {
                    TextButton(onClick = onEdit) { Text("Editar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF4F7FC)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A5EDB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "COMPARTE TU EXPERIENCIA",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Código QR para dejar una reseña de ${business.name}",
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .aspectRatio(1f)
                                .background(Color.White, RoundedCornerShape(18.dp))
                                .padding(12.dp)
                        )
                    } else {
                        Text(
                            "Añade un enlace válido para generar el QR",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = business.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Escanea el código para dejar una reseña",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

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
                    Text(
                        "Información del negocio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10243A)
                    )
                    InfoRow("Categoría", business.category.ifBlank { "Sin categoría" })
                    InfoRow("Dirección", business.address.ifBlank { "Sin dirección" })
                    InfoRow("Teléfono", business.phone.ifBlank { "Sin teléfono" })
                    InfoRow("Correo", business.email.ifBlank { "Sin correo" })
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Enlace de reseñas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10243A)
                    )
                    Text(
                        business.googleReviewUrl.ifBlank { "Sin URL" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF53657A)
                    )
                    HorizontalDivider(color = Color(0xFFE7ECF3))
                    Text("Mensaje sugerido", style = MaterialTheme.typography.labelLarge, color = Color(0xFF53657A))
                    Text(reviewMessage, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF10243A))
                }
            }

            Button(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "${business.name} — reseñas")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartir información"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Compartir información", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    if (business.googleReviewUrl.isNotBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(business.googleReviewUrl)))
                    }
                },
                enabled = business.googleReviewUrl.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Abrir página de reseñas")
            }

            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar negocio")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF718096))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF10243A))
    }
}
