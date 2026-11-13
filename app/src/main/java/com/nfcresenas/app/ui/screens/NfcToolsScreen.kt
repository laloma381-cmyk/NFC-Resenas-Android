package com.nfcresenas.app.ui.screens

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.nio.charset.Charset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcToolsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val adapter = remember { NfcAdapter.getDefaultAdapter(context) }
    var url by remember { mutableStateOf("https://") }
    var writeMode by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Acerca una tarjeta o etiqueta NFC al teléfono.") }
    var lastRead by remember { mutableStateOf("") }
    val currentWriteMode = rememberUpdatedState(writeMode)
    val currentUrl = rememberUpdatedState(url)

    DisposableEffect(adapter, activity) {
        if (adapter != null && activity != null && adapter.isEnabled) {
            val callback = NfcAdapter.ReaderCallback { tag: Tag ->
                val message = runCatching {
                    val ndef = Ndef.get(tag) ?: error("La etiqueta no admite registros NDEF.")
                    ndef.connect()
                    try {
                        if (currentWriteMode.value) {
                            val target = currentUrl.value.trim()
                            val uri = Uri.parse(target)
                            require((uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrBlank()) {
                                "Escribe primero un enlace web válido."
                            }
                            val record = NdefRecord.createUri(uri)
                            val ndefMessage = NdefMessage(arrayOf(record))
                            require(ndef.isWritable) { "La etiqueta está protegida contra escritura." }
                            require(ndefMessage.toByteArray().size <= ndef.maxSize) { "El enlace es demasiado largo para esta etiqueta." }
                            ndef.writeNdefMessage(ndefMessage)
                            "¡Tarjeta programada correctamente!\n$target"
                        } else {
                            val records = ndef.ndefMessage?.records ?: emptyArray()
                            if (records.isEmpty()) "La etiqueta está vacía." else records.joinToString("\n") { record ->
                                record.toUri()?.toString() ?: run {
                                    val payload = record.payload
                                    if (payload.isNotEmpty() && record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                                        record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                                        val languageLength = payload[0].toInt() and 0x3F
                                        String(payload, (1 + languageLength).coerceAtMost(payload.size), (payload.size - 1 - languageLength).coerceAtLeast(0), Charset.forName("UTF-8"))
                                    } else String(payload, Charset.forName("UTF-8"))
                                }
                            }
                        }
                    } finally {
                        ndef.close()
                    }
                }.fold(
                    onSuccess = { it },
                    onFailure = { "No se pudo procesar la etiqueta: ${it.message ?: "error NFC"}" }
                )
                if (currentWriteMode.value) {
                    status = message
                    lastRead = ""
                } else {
                    lastRead = message
                    status = if (message.startsWith("No se pudo")) message else "Lectura completada."
                }
            }
            adapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V,
                null
            )
        } else if (adapter == null) {
            status = "Este dispositivo no dispone de NFC."
        } else if (!adapter.isEnabled) {
            status = "Activa NFC en los ajustes rápidos del teléfono."
        }
        onDispose {
            if (adapter != null && activity != null) runCatching { adapter.disableReaderMode(activity) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Herramientas NFC", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.padding(top = 3.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Acerca la tarjeta al móvil", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Lee el contenido o graba un enlace web. No mostramos datos técnicos que no necesitas.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { writeMode = false; status = "Modo lectura: acerca una etiqueta NFC." }, modifier = Modifier.weight(1f), enabled = adapter != null) {
                    Text("Leer tarjeta")
                }
                Button(onClick = { writeMode = true; status = "Modo grabación: acerca una etiqueta NFC." }, modifier = Modifier.weight(1f), enabled = adapter != null) {
                    Text("Grabar enlace")
                }
            }
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enlace que quieres guardar") },
                placeholder = { Text("https://tu-negocio.com/resenas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                enabled = writeMode
            )
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Estado", fontWeight = FontWeight.Bold)
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                    if (lastRead.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Contenido leído", fontWeight = FontWeight.Bold)
                        Text(lastRead, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Text("La grabación solo funciona con etiquetas compatibles, desbloqueadas y con espacio suficiente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
