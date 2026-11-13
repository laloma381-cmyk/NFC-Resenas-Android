package com.nfcresenas.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nfcresenas.app.data.Business

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessFormScreen(
    business: Business? = null,
    onSave: (Business) -> Unit,
    onCancel: () -> Unit
) {
    var name by rememberSaveable(business?.id) { mutableStateOf(business?.name.orEmpty()) }
    var category by rememberSaveable(business?.id) { mutableStateOf(business?.category.orEmpty()) }
    var address by rememberSaveable(business?.id) { mutableStateOf(business?.address.orEmpty()) }
    var reviewUrl by rememberSaveable(business?.id) { mutableStateOf(business?.googleReviewUrl.orEmpty()) }
    var phone by rememberSaveable(business?.id) { mutableStateOf(business?.phone.orEmpty()) }
    var email by rememberSaveable(business?.id) { mutableStateOf(business?.email.orEmpty()) }
    var instagramUrl by rememberSaveable(business?.id) { mutableStateOf(business?.instagramUrl.orEmpty()) }
    var facebookUrl by rememberSaveable(business?.id) { mutableStateOf(business?.facebookUrl.orEmpty()) }
    var attemptedSave by rememberSaveable(business?.id) { mutableStateOf(false) }

    val normalizedUrl = remember(reviewUrl) {
        val value = reviewUrl.trim()
        if (value.isNotEmpty() && !value.startsWith("http://", ignoreCase = true) && !value.startsWith("https://", ignoreCase = true)) {
            "https://$value"
        } else value
    }
    val validReviewUrl = remember(normalizedUrl) {
        runCatching {
            val uri = Uri.parse(normalizedUrl)
            (uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }
    val normalizedInstagramUrl = remember(instagramUrl) { normalizeSocialUrl(instagramUrl, "instagram.com") }
    val normalizedFacebookUrl = remember(facebookUrl) { normalizeSocialUrl(facebookUrl, "facebook.com") }
    val validInstagramUrl = remember(normalizedInstagramUrl) {
        isOptionalSocialUrlValid(normalizedInstagramUrl, setOf("instagram.com", "www.instagram.com"))
    }
    val validFacebookUrl = remember(normalizedFacebookUrl) {
        isOptionalSocialUrlValid(normalizedFacebookUrl, setOf("facebook.com", "www.facebook.com", "m.facebook.com"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (business == null) "Nuevo negocio" else "Editar negocio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onCancel) {
                        androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Completa la información para preparar el código QR de reseñas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del negocio *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = attemptedSave && name.isBlank(),
                supportingText = if (attemptedSave && name.isBlank()) ({ Text("Escribe el nombre del negocio") }) else null
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = reviewUrl,
                onValueChange = { reviewUrl = it },
                label = { Text("Enlace de reseñas de Google *") },
                placeholder = { Text("https://g.page/r/…") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                isError = attemptedSave && !validReviewUrl,
                supportingText = {
                    if (attemptedSave && !validReviewUrl) Text("Introduce un enlace válido que empiece por https://")
                    else Text("También puedes pegar un enlace sin https://; lo añadiremos automáticamente.")
                }
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            Text("REDES SOCIALES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = instagramUrl,
                onValueChange = { instagramUrl = it },
                label = { Text("Instagram") },
                placeholder = { Text("@tunegocio o enlace del perfil") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                isError = attemptedSave && !validInstagramUrl,
                supportingText = {
                    if (attemptedSave && !validInstagramUrl) Text("Introduce un usuario o enlace válido de Instagram")
                    else Text("Opcional. Puedes escribir @usuario o pegar el enlace.")
                }
            )
            OutlinedTextField(
                value = facebookUrl,
                onValueChange = { facebookUrl = it },
                label = { Text("Facebook") },
                placeholder = { Text("Nombre de página o enlace") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                isError = attemptedSave && !validFacebookUrl,
                supportingText = {
                    if (attemptedSave && !validFacebookUrl) Text("Introduce un nombre o enlace válido de Facebook")
                    else Text("Opcional. Puedes escribir el nombre de la página o pegar el enlace.")
                }
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    attemptedSave = true
                    if (name.isNotBlank() && validReviewUrl && validInstagramUrl && validFacebookUrl) {
                        val saved = business?.copy(
                            name = name.trim(),
                            category = category.trim(),
                            address = address.trim(),
                            googleReviewUrl = normalizedUrl,
                            phone = phone.trim(),
                            email = email.trim(),
                            instagramUrl = normalizedInstagramUrl,
                            facebookUrl = normalizedFacebookUrl
                        ) ?: Business(
                            name = name.trim(),
                            category = category.trim(),
                            address = address.trim(),
                            googleReviewUrl = normalizedUrl,
                            phone = phone.trim(),
                            email = email.trim(),
                            instagramUrl = normalizedInstagramUrl,
                            facebookUrl = normalizedFacebookUrl
                        )
                        onSave(saved)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Guardar negocio", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancelar")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun normalizeSocialUrl(rawValue: String, domain: String): String {
    val value = rawValue.trim()
    if (value.isBlank()) return ""
    if (value.startsWith("https://", ignoreCase = true) || value.startsWith("http://", ignoreCase = true)) return value
    val username = value.removePrefix("@").trim().trim('/')
    return if (username.isBlank()) "" else "https://$domain/$username"
}

private fun isOptionalSocialUrlValid(value: String, allowedHosts: Set<String>): Boolean {
    if (value.isBlank()) return true
    return runCatching {
        val uri = Uri.parse(value)
        (uri.scheme == "https" || uri.scheme == "http") && uri.host?.lowercase()?.let { it in allowedHosts } == true
    }.getOrDefault(false)
}
