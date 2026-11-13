package com.nfcresenas.app.ui.screens

import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nfcresenas.app.MainApplication
import com.nfcresenas.app.data.NfcHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

private const val TYPE_URL = "Enlace"
private const val TYPE_TEXT = "Texto"
private const val TYPE_CONTACT = "Contacto"
private const val TYPE_WIFI = "Wi-Fi"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcToolsScreen(initialUrl: String = "https://", onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val adapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val historyDao = remember { (context.applicationContext as MainApplication).database.nfcHistoryDao() }
    val history by historyDao.getRecent().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var url by remember(initialUrl) { mutableStateOf(initialUrl) }
    var recordType by remember { mutableStateOf(TYPE_URL) }
    var writeMode by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Acerca una tarjeta o etiqueta NFC al teléfono.") }
    var lastRead by remember { mutableStateOf("") }
    var textValue by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var wifiName by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiOpen by remember { mutableStateOf(false) }

    val currentWriteMode = rememberUpdatedState(writeMode)
    val currentType = rememberUpdatedState(recordType)
    val currentUrl = rememberUpdatedState(url)
    val currentText = rememberUpdatedState(textValue)
    val currentContactName = rememberUpdatedState(contactName)
    val currentContactPhone = rememberUpdatedState(contactPhone)
    val currentContactEmail = rememberUpdatedState(contactEmail)
    val currentWifiName = rememberUpdatedState(wifiName)
    val currentWifiPassword = rememberUpdatedState(wifiPassword)
    val currentWifiOpen = rememberUpdatedState(wifiOpen)

    DisposableEffect(adapter, activity) {
        if (adapter != null && activity != null && adapter.isEnabled) {
            val callback = NfcAdapter.ReaderCallback { tag: Tag ->
                val result = runCatching {
                    val ndef = Ndef.get(tag) ?: error("La etiqueta no admite registros NDEF.")
                    ndef.connect()
                    try {
                        if (currentWriteMode.value) {
                            val type = currentType.value
                            val records = when (type) {
                                TYPE_URL -> {
                                    val target = currentUrl.value.trim()
                                    val uri = Uri.parse(target)
                                    require((uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrBlank()) {
                                        "Escribe primero un enlace web válido."
                                    }
                                    arrayOf(NdefRecord.createUri(uri)) to target
                                }
                                TYPE_TEXT -> {
                                    val value = currentText.value.trim()
                                    require(value.isNotBlank()) { "Escribe el texto que quieres guardar." }
                                    arrayOf(createTextRecord(value)) to value
                                }
                                TYPE_CONTACT -> {
                                    val name = currentContactName.value.trim()
                                    val phone = currentContactPhone.value.trim()
                                    val email = currentContactEmail.value.trim()
                                    require(name.isNotBlank() || phone.isNotBlank() || email.isNotBlank()) {
                                        "Añade al menos nombre, teléfono o correo."
                                    }
                                    val vcard = buildString {
                                        appendLine("BEGIN:VCARD")
                                        appendLine("VERSION:3.0")
                                        if (name.isNotBlank()) {
                                            appendLine("FN:$name")
                                            appendLine("N:$name;;;;")
                                        }
                                        if (phone.isNotBlank()) appendLine("TEL;TYPE=CELL:$phone")
                                        if (email.isNotBlank()) appendLine("EMAIL:$email")
                                        append("END:VCARD")
                                    }
                                    arrayOf(NdefRecord.createMime("text/vcard", vcard.toByteArray(Charsets.UTF_8))) to vcard
                                }
                                TYPE_WIFI -> {
                                    val ssid = currentWifiName.value.trim()
                                    val password = currentWifiPassword.value
                                    require(ssid.isNotBlank()) { "Escribe el nombre de la red Wi-Fi." }
                                    require(currentWifiOpen.value || password.length >= 8) {
                                        "La contraseña WPA2 debe tener al menos 8 caracteres."
                                    }
                                    val payload = buildWifiWscPayload(ssid, password, currentWifiOpen.value)
                                    arrayOf(NdefRecord.createMime("application/vnd.wfa.wsc", payload)) to
                                        "Red: $ssid\nSeguridad: ${if (currentWifiOpen.value) "Abierta" else "WPA2"}"
                                }
                                else -> error("Tipo de contenido no válido.")
                            }
                            val message = NdefMessage(records.first)
                            require(ndef.isWritable) { "La etiqueta está protegida contra escritura." }
                            require(message.toByteArray().size <= ndef.maxSize) {
                                "El contenido es demasiado grande para esta etiqueta."
                            }
                            ndef.writeNdefMessage(message)
                            scope.launch(Dispatchers.IO) {
                                historyDao.insert(NfcHistory(action = "Grabado", recordType = type, content = records.second))
                            }
                            "¡$type grabado correctamente!\nAcerca otra etiqueta para grabar de nuevo."
                        } else {
                            val records = ndef.ndefMessage?.records ?: emptyArray()
                            val decoded = if (records.isEmpty()) "La etiqueta está vacía." else records.joinToString("\n\n") { record ->
                                when {
                                    record.toUri() != null -> record.toUri().toString()
                                    record.toMimeType() == "text/vcard" -> String(record.payload, Charsets.UTF_8)
                                    record.toMimeType() == "application/vnd.wfa.wsc" -> decodeWifiSummary(record.payload)
                                    record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                                        record.type.contentEquals(NdefRecord.RTD_TEXT) -> decodeTextRecord(record.payload)
                                    else -> String(record.payload, Charsets.UTF_8).ifBlank { "Contenido NFC no reconocido" }
                                }
                            }
                            scope.launch(Dispatchers.IO) {
                                historyDao.insert(NfcHistory(action = "Leído", recordType = "NFC", content = decoded))
                            }
                            decoded
                        }
                    } finally {
                        ndef.close()
                    }
                }.fold(
                    onSuccess = { it },
                    onFailure = { "No se pudo procesar la etiqueta: ${it.message ?: "error NFC"}" }
                )
                if (currentWriteMode.value) {
                    status = result
                    lastRead = ""
                } else {
                    lastRead = result
                    status = if (result.startsWith("No se pudo")) result else "Lectura completada."
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
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.padding(top = 3.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Acerca la tarjeta al móvil", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Lee etiquetas o graba enlaces, texto, contactos y redes Wi-Fi.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { writeMode = false; status = "Modo lectura: acerca una etiqueta NFC." }, modifier = Modifier.weight(1f), enabled = adapter != null) {
                    Text("Leer tarjeta")
                }
                Button(onClick = { writeMode = true; status = "Modo grabación: elige el contenido y acerca una etiqueta." }, modifier = Modifier.weight(1f), enabled = adapter != null) {
                    Text("Grabar tarjeta")
                }
            }
            if (writeMode) {
                Text("QUÉ QUIERES GRABAR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TYPE_URL, TYPE_TEXT).forEach { type ->
                        TextButton(onClick = { recordType = type }) {
                            Text(if (recordType == type) "✓ $type" else type)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TYPE_CONTACT, TYPE_WIFI).forEach { type ->
                        TextButton(onClick = { recordType = type }) {
                            Text(if (recordType == type) "✓ $type" else type)
                        }
                    }
                }
                when (recordType) {
                    TYPE_URL -> OutlinedTextField(
                        value = url, onValueChange = { url = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enlace web") }, placeholder = { Text("https://tu-negocio.com/resenas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri), singleLine = true
                    )
                    TYPE_TEXT -> OutlinedTextField(
                        value = textValue, onValueChange = { textValue = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Texto que guardar") }, minLines = 3
                    )
                    TYPE_CONTACT -> {
                        OutlinedTextField(value = contactName, onValueChange = { contactName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, singleLine = true)
                        OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                        OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Correo electrónico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)
                        Text("Se guardará como contacto vCard, compatible con la mayoría de móviles.")
                    }
                    TYPE_WIFI -> {
                        OutlinedTextField(value = wifiName, onValueChange = { wifiName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre de la red (SSID)") }, singleLine = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { wifiOpen = false }) { Text(if (!wifiOpen) "✓ WPA2" else "WPA2") }
                            TextButton(onClick = { wifiOpen = true; wifiPassword = "" }) { Text(if (wifiOpen) "✓ Red abierta" else "Red abierta") }
                        }
                        if (!wifiOpen) {
                            OutlinedTextField(value = wifiPassword, onValueChange = { wifiPassword = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Contraseña Wi-Fi") }, singleLine = true)
                        }
                        Text("Se grabará un registro Wi-Fi WSC. La compatibilidad depende del lector NFC del otro dispositivo.")
                    }
                }
            } else {
                Text("Modo lectura activo: acerca una etiqueta NFC para ver su contenido.")
            }
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Estado", fontWeight = FontWeight.Bold)
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                    if (lastRead.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Contenido leído", fontWeight = FontWeight.Bold)
                        Text(lastRead, style = MaterialTheme.typography.bodyMedium)
                        val uri = runCatching { Uri.parse(lastRead) }.getOrNull()
                        if (uri?.scheme == "https" || uri?.scheme == "http") {
                            Button(onClick = {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                            }) { Text("Abrir enlace leído") }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Historial reciente", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { scope.launch(Dispatchers.IO) { historyDao.clear() } }, enabled = history.isNotEmpty()) {
                    Text("Borrar historial")
                }
            }
            if (history.isEmpty()) {
                Text("Todavía no hay lecturas ni grabaciones guardadas.", style = MaterialTheme.typography.bodySmall)
            } else {
                history.take(10).forEach { item ->
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("${item.action} · ${item.recordType}", fontWeight = FontWeight.SemiBold)
                            Text(item.content, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                            Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.createdAt)), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Text("La grabación requiere etiquetas compatibles, desbloqueadas y con espacio suficiente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun createTextRecord(value: String): NdefRecord {
    val language = "es".toByteArray(Charsets.US_ASCII)
    val text = value.toByteArray(Charsets.UTF_8)
    val payload = byteArrayOf(language.size.toByte()) + language + text
    return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), payload)
}

private fun decodeTextRecord(payload: ByteArray): String {
    if (payload.isEmpty()) return ""
    val languageLength = payload[0].toInt() and 0x3F
    val start = (1 + languageLength).coerceAtMost(payload.size)
    return String(payload, start, payload.size - start, Charsets.UTF_8)
}

private fun buildWifiWscPayload(ssid: String, password: String, open: Boolean): ByteArray {
    fun attribute(id: Int, value: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        out.write((id shr 8) and 0xFF)
        out.write(id and 0xFF)
        out.write((value.size shr 8) and 0xFF)
        out.write(value.size and 0xFF)
        out.write(value)
        return out.toByteArray()
    }
    val version = attribute(0x104A, byteArrayOf(0x10))
    val ssidBytes = ssid.toByteArray(Charsets.UTF_8)
    val networkName = attribute(0x1045, ssidBytes)
    val authType = attribute(0x1003, if (open) byteArrayOf(0x00, 0x01) else byteArrayOf(0x00, 0x20))
    val encryption = attribute(0x100F, if (open) byteArrayOf(0x00, 0x01) else byteArrayOf(0x00, 0x08))
    val networkKey = if (open) byteArrayOf() else attribute(0x1027, password.toByteArray(Charsets.UTF_8))
    val macAddress = attribute(0x1020, ByteArray(6))
    val credential = attribute(0x100E, networkName + authType + encryption + networkKey + macAddress)
    return version + credential
}

private fun decodeWifiSummary(payload: ByteArray): String {
    var index = 0
    while (index + 4 <= payload.size) {
        val id = ((payload[index].toInt() and 0xFF) shl 8) or (payload[index + 1].toInt() and 0xFF)
        val length = ((payload[index + 2].toInt() and 0xFF) shl 8) or (payload[index + 3].toInt() and 0xFF)
        index += 4
        if (length < 0 || index + length > payload.size) break
        if (id == 0x100E) {
            val nested = payload.copyOfRange(index, index + length)
            var nestedIndex = 0
            while (nestedIndex + 4 <= nested.size) {
                val nestedId = ((nested[nestedIndex].toInt() and 0xFF) shl 8) or (nested[nestedIndex + 1].toInt() and 0xFF)
                val nestedLength = ((nested[nestedIndex + 2].toInt() and 0xFF) shl 8) or (nested[nestedIndex + 3].toInt() and 0xFF)
                nestedIndex += 4
                if (nestedLength < 0 || nestedIndex + nestedLength > nested.size) break
                if (nestedId == 0x1045) return "Red Wi-Fi: ${String(nested, nestedIndex, nestedLength, Charsets.UTF_8)}\nRegistro Wi-Fi WSC"
                nestedIndex += nestedLength
            }
        }
        index += length
    }
    return "Registro de configuración Wi-Fi WSC"
}
