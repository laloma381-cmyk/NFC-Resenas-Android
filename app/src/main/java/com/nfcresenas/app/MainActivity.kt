package com.nfcresenas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.nfcresenas.app.data.Business
import com.nfcresenas.app.data.BusinessRepository
import com.nfcresenas.app.ui.BusinessViewModel
import com.nfcresenas.app.ui.BusinessViewModelFactory
import com.nfcresenas.app.ui.screens.BusinessDetailScreen
import com.nfcresenas.app.ui.screens.BusinessFormScreen
import com.nfcresenas.app.ui.screens.BusinessListScreen
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

                if (showSplash) {
                    SplashScreen {
                        showSplash = false
                    }
                } else if (showForm) {
                    BusinessFormScreen(
                        business = selectedBusiness,
                        onSave = { business ->
                            if (business.id == 0L) {
                                viewModel.saveBusiness(business)
                            } else {
                                viewModel.updateBusiness(business)
                            }
                            showForm = false
                            selectedBusiness = null
                        },
                        onCancel = {
                            showForm = false
                            selectedBusiness = null
                        }
                    )
                } else if (selectedBusiness != null) {
                    BusinessDetailScreen(
                        business = selectedBusiness!!,
                        onBack = { selectedBusiness = null },
                        onEdit = { showForm = true },
                        onDelete = {
                            viewModel.deleteBusiness(selectedBusiness!!)
                            selectedBusiness = null
                        }
                    )
                } else {
                    BusinessListScreen(
                        businesses = uiState.businesses,
                        onAddBusiness = {
                            selectedBusiness = null
                            showForm = true
                        },
                        onSelectBusiness = { selectedBusiness = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_nfc_resenas),
            contentDescription = "Logo NFC Reseñas",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
