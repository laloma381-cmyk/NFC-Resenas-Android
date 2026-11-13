package com.nfcresenas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.nfcresenas.app.data.Business
import com.nfcresenas.app.data.BusinessRepository
import com.nfcresenas.app.ui.BusinessViewModel
import com.nfcresenas.app.ui.BusinessViewModelFactory
import com.nfcresenas.app.ui.screens.BusinessDetailScreen
import com.nfcresenas.app.ui.screens.BusinessFormScreen
import com.nfcresenas.app.ui.screens.BusinessListScreen
import com.nfcresenas.app.ui.screens.NfcToolsScreen
import com.nfcresenas.app.ui.screens.ToolsHomeScreen
import com.nfcresenas.app.ui.theme.NFCResenasTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val database by lazy { (application as MainApplication).database }
    private val repository by lazy { BusinessRepository(database.businessDao()) }

    private val viewModel: BusinessViewModel by viewModels {
        BusinessViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NFCResenasTheme {
                val uiState by viewModel.uiState.collectAsState()
                var selectedBusiness by remember { mutableStateOf<Business?>(null) }
                var showForm by remember { mutableStateOf(false) }
                var showSplash by remember { mutableStateOf(true) }
                var showHome by remember { mutableStateOf(true) }
                var showNfcTools by remember { mutableStateOf(false) }
                var nfcInitialUrl by remember { mutableStateOf("https://") }
                var showBusinesses by remember { mutableStateOf(false) }

                if (showSplash) {
                    SplashScreen { showSplash = false }
                } else if (showNfcTools) {
                    NfcToolsScreen(initialUrl = nfcInitialUrl, onBack = { showNfcTools = false; showHome = true })
                } else if (showBusinesses && showForm) {
                    BusinessFormScreen(
                        business = selectedBusiness,
                        onSave = { business ->
                            if (business.id == 0L) viewModel.saveBusiness(business)
                            else viewModel.updateBusiness(business)
                            showForm = false
                            selectedBusiness = null
                        },
                        onCancel = {
                            showForm = false
                            selectedBusiness = null
                        }
                    )
                } else if (showBusinesses && selectedBusiness != null) {
                    BusinessDetailScreen(
                        business = selectedBusiness!!,
                        onBack = { selectedBusiness = null },
                        onEdit = { showForm = true },
                        onDelete = {
                            viewModel.deleteBusiness(selectedBusiness!!)
                            selectedBusiness = null
                        },
                        onUpdateBusiness = { viewModel.updateBusiness(it) },
                        onProgramNfc = { url ->
                            nfcInitialUrl = url
                            showBusinesses = false
                            showNfcTools = true
                        }
                    )
                } else if (showBusinesses) {
                    BusinessListScreen(
                        businesses = uiState.businesses,
                        onAddBusiness = {
                            selectedBusiness = null
                            showForm = true
                        },
                        onSelectBusiness = { selectedBusiness = it },
                        onBack = { showBusinesses = false; showHome = true }
                    )
                } else if (showHome) {
                    ToolsHomeScreen(
                        onOpenBusinesses = {
                            showBusinesses = true
                            showHome = false
                        },
                        onOpenNfc = {
                            showNfcTools = true
                            showHome = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF101B36)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_icon),
                contentDescription = "Logotipo de ReviewQR Manager",
                modifier = Modifier.size(210.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(18.dp))
            androidx.compose.material3.Text(
                "ReviewQR Manager",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )
            androidx.compose.material3.Text(
                "QR · NFC · Negocios",
                color = Color(0xFFFFC45B),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }
    }
}
