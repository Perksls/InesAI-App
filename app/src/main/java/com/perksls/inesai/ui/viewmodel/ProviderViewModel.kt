package com.perksls.inesai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perksls.inesai.data.model.AIProvider
import com.perksls.inesai.data.repository.ProviderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProviderViewModel(
    private val repository: ProviderRepository
) : ViewModel() {

    val providers: StateFlow<List<AIProvider>> = repository.getAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveProvider(
        name: String,
        baseUrl: String,
        apiKey: String,
        models: List<String>,
        isOpenAICompatible: Boolean = true,
        id: String? = null
    ) {
        viewModelScope.launch {
            if (id != null) {
                repository.updateProvider(id, name, baseUrl, apiKey, models, isOpenAICompatible)
            } else {
                val newProvider = repository.saveProvider(name, baseUrl, apiKey, models, isOpenAICompatible)
                if (providers.value.isEmpty()) {
                    repository.setPrimary(newProvider.id)
                }
            }
        }
    }

    fun setActiveModel(providerId: String, model: String) {
        viewModelScope.launch {
            repository.setActiveModel(providerId, model)
        }
    }

    fun deleteProvider(id: String) {
        viewModelScope.launch {
            repository.deleteProvider(id)
        }
    }

    fun setPrimary(id: String) {
        viewModelScope.launch {
            repository.setPrimary(id)
        }
    }

    fun reorderProviders(orderedIds: List<String>) {
        viewModelScope.launch {
            repository.reorderProviders(orderedIds)
        }
    }
}
