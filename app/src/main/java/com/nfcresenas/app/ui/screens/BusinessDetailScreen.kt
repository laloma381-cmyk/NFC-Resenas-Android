package com.nfcresenas.app.ui.screens

import android.content.ClipData
import android.content.Intent
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.nfcresenas.app.R
import com.nfcresenas.app.data.Business
import com.nfcresenas.app.ui.QRCodeGenerator
import com.nfcresenas.app.ui.QrPalette
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
    var selectedPaletteId by rememberSaveable(business.id) { mutableStateOf(QRCodeGenerator.palettes.first().id) }
    val palette = QRCodeGenerator.palettes.firstOrNull { it.id == selectedPaletteId } ?: QRCodeGenerator.palettes.first()
    val designedQr = remember(business.googleReviewUrl, business.name, palette.id) {
        business.googleReviewUrl.takeIf(String::isNotBlank)?.let {
            QRCodeGenerator.generateDesignedQrBitmap(it, business.name, palette)
        }
    }
    val reviewMessage = remember(business.name) { QRCodeGenerator.buildReviewMessage(business.name) }
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
                    Text(business.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, color = Ink)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Ink)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar negocio", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Page)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("DISEÑO PERSONALIZADO", style = MaterialTheme.typography.labelLarge, color = BrandBlue, fontWeight = FontWeight.Bold)
                        Text("Tu QR, tu estilo", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
                        Text("Elige una paleta y comparte una tarjeta lista para imprimir. El QR mantiene alto contraste para escanearlo fácilmente.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        QRCodeGenerator.palettes.forEach { option ->
                            PaletteChip(option, selected = option.id == palette.id) {
                                selectedPaletteId = option.id
                            }
                        }
                    }

                    if (designedQr != null) {
                        Image(
                            bitmap = designedQr.asImageBitmap(),
                            contentDescription = "Diseño de código QR para ${business.name}, paleta ${palette.name}",
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.8f).clip(RoundedCornerShape(18.dp))
                        )
                        Button(
                            onClick = {
                                try {
                                    val directory = File(context.cacheDir, "shared_qr").apply { mkdirs() }
                                    val safeName = business.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "negocio" }
                                    val imageFile = File(directory, "qr-resenas-$safeName-${palette.id}.png")
                                    imageFile.outputStream().use { output ->
                                        check(designedQr.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output))
                                    }
                                    val imageUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, imageUri)
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        clipData = ClipData.newRawUri("QR diseñado de ${business.name}", imageUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir QR diseñado"))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No se pudo preparar el diseño para compartir", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Compartir QR en PNG", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                val document = PdfDocument()
                                try {
                                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                                    val page = document.startPage(pageInfo)
                                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                                    page.canvas.drawColor(android.graphics.Color.WHITE)
                                    page.canvas.drawBitmap(designedQr, null, RectF(28f, 55f, 567f, 729f), paint)
                                    document.finishPage(page)
                                    val directory = File(context.cacheDir, "shared_qr").apply { mkdirs() }
                                    val safeName = business.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "negocio" }
                                    val pdfFile = File(directory, "qr-resenas-$safeName.pdf")
                                    pdfFile.outputStream().use { output -> document.writeTo(output) }
                                    val pdfUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                                        putExtra(Intent.EXTRA_TEXT, "Código QR de reseñas de ${business.name}")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir QR en PDF"))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No se pudo generar el PDF", Toast.LENGTH_LONG).show()
                                } finally {
                                    document.close()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Exportar también en PDF", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text("Añade un enlace válido para crear tu QR personalizado.", color = Muted)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
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
                ) { Text("Compartir datos", fontWeight = FontWeight.SemiBold) }
                OutlinedButton(
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(business.googleReviewUrl)))
                        } catch (_: Exception) {
                            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = business.googleReviewUrl.isNotBlank(),
                    modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Abrir reseñas", fontWeight = FontWeight.SemiBold) }
            }

            InfoCard(title = "Información del negocio") {
                InfoRow("Categoría", business.category.ifBlank { "Sin categoría" })
                InfoRow("Dirección", business.address.ifBlank { "Sin dirección" })
                InfoRow("Teléfono", business.phone.ifBlank { "Sin teléfono" })
                InfoRow("Correo", business.email.ifBlank { "Sin correo" })
            }

            InfoCard(title = "Mensaje sugerido") {
                Text(reviewMessage, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }

            androidx.compose.material3.TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Eliminar negocio") }
        }
    }
}

@Composable
private fun PaletteChip(palette: QrPalette, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(palette.name) },
        leadingIcon = {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(14.dp).clip(CircleShape).background(Color(palette.primary))
            )
        }
    )
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
        HorizontalDivider(color = Color(0xFFE7ECF3))
    }
}
