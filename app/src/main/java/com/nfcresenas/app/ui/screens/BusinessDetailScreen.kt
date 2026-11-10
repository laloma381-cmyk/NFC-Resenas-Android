package com.nfcresenas.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcresenas.app.data.Business
import com.nfcresenas.app.ui.QRCodeGenerator

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(business.name) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Atrás")
                    }
                },
                actions = {
                    TextButton(onClick = onEdit) {
                        Text("Editar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (qrBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A5EDB), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR de reseñas",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Categoría", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = business.category.ifBlank { "Sin categoría" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text("Dirección", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = business.address.ifBlank { "Sin dirección" },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text("Teléfono", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = business.phone.ifBlank { "Sin teléfono" },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text("Correo", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = business.email.ifBlank { "Sin correo" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Enlace de reseñas", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = business.googleReviewUrl.ifBlank { "Sin URL" },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("Mensaje sugerido", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = reviewMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    val uri = Uri.parse(business.googleReviewUrl)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir reseñas")
            }

            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar negocio")
            }
        }
    }
}
