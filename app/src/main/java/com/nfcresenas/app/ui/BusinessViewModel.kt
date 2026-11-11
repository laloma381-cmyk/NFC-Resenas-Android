package com.nfcresenas.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nfcresenas.app.data.Business
import com.nfcresenas.app.data.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BusinessUiState(
    val businesses: List<Business> = emptyList()
)

class BusinessViewModel(
    private val repository: BusinessRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BusinessUiState())
    val uiState: StateFlow<BusinessUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllBusinesses().collect { businesses ->
                _uiState.value = _uiState.value.copy(businesses = businesses)
            }
        }
    }

    fun saveBusiness(business: Business) {
        viewModelScope.launch {
            repository.saveBusiness(business)
        }
    }

    fun updateBusiness(business: Business) {
        viewModelScope.launch {
            repository.updateBusiness(business)
        }
    }

    fun deleteBusiness(business: Business) {
        viewModelScope.launch {
            repository.deleteBusiness(business)
        }
    }
}

class BusinessViewModelFactory(
    private val repository: BusinessRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusinessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
