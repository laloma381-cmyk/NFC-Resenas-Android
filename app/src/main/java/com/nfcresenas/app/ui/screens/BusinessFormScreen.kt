package com.nfcresenas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfcresenas.app.data.Business

@Composable
fun BusinessFormScreen(
    business: Business? = null,
    onSave: (Business) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(business?.name ?: "") }
    var category by remember { mutableStateOf(business?.category ?: "") }
    var address by remember { mutableStateOf(business?.address ?: "") }
    var googleReviewUrl by remember { mutableStateOf(business?.googleReviewUrl ?: "") }
    var phone by remember { mutableStateOf(business?.phone ?: "") }
    var email by remember { mutableStateOf(business?.email ?: "") }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (business == null) "Nuevo negocio" else "Editar negocio",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = googleReviewUrl,
                onValueChange = { googleReviewUrl = it },
                label = { Text("URL de reseñas") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        val saved = business?.copy(
                            name = name,
                            category = category,
                            address = address,
                            googleReviewUrl = googleReviewUrl,
                            phone = phone,
                            email = email
                        ) ?: Business(
                            name = name,
                            category = category,
                            address = address,
                            googleReviewUrl = googleReviewUrl,
                            phone = phone,
                            email = email
                        )
                        onSave(saved)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
