package com.nfcresenas.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfcresenas.app.data.Business

private val BrandBlue = Color(0xFF0A5EDB)
private val PageBackground = Color(0xFFF3F7FD)
private val MutedText = Color(0xFF66758A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessListScreen(
    businesses: List<Business>,
    onAddBusiness: () -> Unit,
    onSelectBusiness: (Business) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredBusinesses = remember(businesses, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) businesses
        else businesses.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandBlue)
                        Text("NFC Reseñas", fontWeight = FontWeight.Bold)
                    }
                }
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("GESTIÓN DE NEGOCIOS", style = MaterialTheme.typography.labelLarge, color = BrandBlue, fontWeight = FontWeight.Bold)
                    Text("Tus negocios", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        if (businesses.size == 1) "1 negocio listo para recibir reseñas" else "${businesses.size} negocios listos para recibir reseñas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Buscar por nombre o categoría") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(42.dp))
                            Text(
                                if (businesses.isEmpty()) "Empieza con tu primer negocio" else "No hay resultados",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (businesses.isEmpty()) "Guarda los datos y el enlace de reseñas para crear un QR que puedas compartir." else "Prueba con otro nombre o categoría.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedText
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFE7F0FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = business.name.trim().take(1).uppercase().ifBlank { "N" },
                    color = BrandBlue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(business.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(business.category.ifBlank { "Sin categoría" }, style = MaterialTheme.typography.bodyMedium, color = MutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (business.googleReviewUrl.isBlank()) "Añade el enlace de reseñas" else "Enlace de reseñas configurado",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (business.googleReviewUrl.isBlank()) MutedText else Color(0xFF16834A)
                )
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = MutedText)
        }
    }
}
