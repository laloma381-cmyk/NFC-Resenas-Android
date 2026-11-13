package com.nfcresenas.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

private val Brand = Color(0xFF315BEA)
private val Ink = Color(0xFF14213D)
private val Muted = Color(0xFF69768C)
private val Page = Color(0xFFF4F6FC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHomeScreen(
    onOpenBusinesses: () -> Unit,
    onOpenNfc: () -> Unit
) {
    val context = LocalContext.current
    var scanResult by remember { mutableStateOf<String?>(null) }
    val scanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        scanResult = result.contents
    }

    Scaffold(
        containerColor = Page,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(38.dp).background(Color(0xFF14213D), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color(0xFFFFC45B))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text("ReviewQR Manager", fontWeight = FontWeight.ExtraBold, color = Ink)
                            Text("Herramientas para tu negocio", style = MaterialTheme.typography.labelSmall, color = Muted)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14213D))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Todo tu negocio, a un toque", color = Color(0xFFFFC45B), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("Crea, lee y comparte", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Gestiona tus negocios, códigos QR y tarjetas NFC desde un solo lugar.", color = Color(0xFFE0E7F5), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(2.dp))
                    Button(
                        onClick = { scanner.launch(ScanOptions().setPrompt("Apunta al código QR").setBeepEnabled(false).setOrientationLocked(false)) },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Escanear un QR")
                    }
                }
            }

            Text("HERRAMIENTAS", style = MaterialTheme.typography.labelLarge, color = Brand, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    listOf(
                        ToolItem("Mis negocios", "Clientes y enlaces", Icons.Default.Business, Color(0xFFEAF0FF), onOpenBusinesses),
                        ToolItem("Leer / grabar NFC", "Programar tarjetas", Icons.Default.Nfc, Color(0xFFE7F8F2), onOpenNfc),
                        ToolItem("Crear códigos QR", "Diseñar y compartir", Icons.Default.QrCode2, Color(0xFFFFF3E2), onOpenBusinesses)
                    )
                ) { item ->
                    Card(
                        onClick = item.action,
                        modifier = Modifier.fillMaxWidth().height(142.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Box(modifier = Modifier.size(42.dp).background(item.tint, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(item.icon, contentDescription = null, tint = Brand, modifier = Modifier.size(23.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(item.title, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(item.subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (scanResult != null) {
        AlertDialog(
            onDismissRequest = { scanResult = null },
            title = { Text("Resultado del QR") },
            text = { Text(scanResult.orEmpty()) },
            confirmButton = {
                TextButton(onClick = {
                    val value = scanResult.orEmpty()
                    runCatching {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(value))
                        context.startActivity(intent)
                    }
                    scanResult = null
                }) { Text("Abrir enlace") }
            },
            dismissButton = {
                TextButton(onClick = { scanResult = null }) { Text("Cerrar") }
            }
        )
    }
}

private data class ToolItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
    val action: () -> Unit
)
