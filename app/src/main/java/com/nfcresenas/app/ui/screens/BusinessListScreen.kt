package com.nfcresenas.app.ui.screens

import android.content.Intent
import android.net.Uri

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalContext
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfcresenas.app.data.Business

private val BrandBlue = Color(0xFF245BDB)
private val DeepBlue = Color(0xFF142B62)
private val PageBackground = Color(0xFFF4F6FB)
private val MutedText = Color(0xFF718096)
private val Success = Color(0xFF16845B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessListScreen(
    businesses: List<Business>,
    onAddBusiness: () -> Unit,
    onSelectBusiness: (Business) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var scannedValue by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val qrScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents?.trim()
        if (!contents.isNullOrBlank()) {
            val uri = runCatching { Uri.parse(contents) }.getOrNull()
            if (uri != null && (uri.scheme.equals("https", true) || uri.scheme.equals("http", true)) && !uri.host.isNullOrBlank()) {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }.onFailure { scannedValue = contents }
            } else {
                scannedValue = contents
            }
        }
    }
    val filteredBusinesses = remember(businesses, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) businesses
        else businesses.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true)
        }
    }

    if (scannedValue != null) {
        AlertDialog(
            onDismissRequest = { scannedValue = null },
            title = { Text("Contenido del código QR") },
            text = { Text(scannedValue.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { scannedValue = null }) { Text("Cerrar") }
            }
        )
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                                .background(BrandBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text("ReviewQR Manager", fontWeight = FontWeight.ExtraBold, color = DeepBlue)
                            Text("Panel de negocios", style = MaterialTheme.typography.labelSmall, color = MutedText)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBusiness,
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir negocio")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Brush.linearGradient(listOf(BrandBlue, DeepBlue)))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
                                Text("TU NEGOCIO, MÁS VISIBLE", modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Text("Gestiona tus reseñas\ncon un solo toque", style = MaterialTheme.typography.headlineSmall,
                                color = Color.White, fontWeight = FontWeight.ExtraBold)
                            Text("Prepara códigos QR personalizados y compártelos fácilmente con tus clientes.",
                                style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(12.dp)) {
                                    Text("${businesses.size} negocios", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                        color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                }
                                Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(12.dp)) {
                                    Text("${businesses.count { it.googleReviewUrl.isNotBlank() }} con enlace QR",
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                        color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Button(
                        onClick = {
                            qrScanner.launch(
                                ScanOptions()
                                    .setPrompt("Enfoca el código QR dentro del recuadro")
                                    .setBeepEnabled(false)
                                    .setOrientationLocked(false)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Escanear QR")
                    }
                    Text("Tus negocios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = DeepBlue)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(17.dp),
                        placeholder = { Text("Buscar negocio, categoría o dirección") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandBlue) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                                }
                            }
                        }
                    )
                }
            }

            if (filteredBusinesses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFEAF0FF)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(30.dp))
                            }
                            Text(
                                if (businesses.isEmpty()) "Tu primer negocio empieza aquí" else "No encontramos resultados",
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DeepBlue
                            )
                            Text(
                                if (businesses.isEmpty()) "Añade un negocio para crear su QR de reseñas y preparar el material para compartir."
                                else "Prueba con otro nombre, categoría o dirección.",
                                style = MaterialTheme.typography.bodyMedium, color = MutedText
                            )
                        }
                    }
                }
            } else {
                items(filteredBusinesses, key = { it.id }) { business ->
                    BusinessCard(business = business, onClick = { onSelectBusiness(business) })
                }
            }
        }
    }
}

@Composable
private fun BusinessCard(business: Business, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFFEAF0FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = business.name.trim().take(1).uppercase().ifBlank { "N" },
                    color = BrandBlue, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(business.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = DeepBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(business.category.ifBlank { "Sin categoría" }, style = MaterialTheme.typography.bodySmall,
                    color = MutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Surface(
                    color = if (business.googleReviewUrl.isBlank()) Color(0xFFFFF4E5) else Color(0xFFE8F6EF),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        if (business.googleReviewUrl.isBlank()) "Falta enlace de reseñas" else "QR listo para diseñar",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (business.googleReviewUrl.isBlank()) Color(0xFF9A5B12) else Success,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = MutedText)
        }
    }
}
