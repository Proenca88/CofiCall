package com.example.coficall.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coficall.data.DataRepository
import com.example.coficall.model.Collaborator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DataRepository) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
    val currentUserEmail: StateFlow<String?> = repository.currentUserEmail
    val isOfflineMode: StateFlow<Boolean> = repository.isOfflineMode
    val isMockMode: StateFlow<Boolean> = repository.isMockMode
    
    val collaborators: StateFlow<List<Collaborator>> = repository.collaborators
    val businessUnits = repository.businessUnits

    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }

    fun login(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = repository.login(email, password)
            onResult(res)
        }
    }

    fun register(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = repository.register(email, password)
            onResult(res)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun toggleFavorite(collaborator: Collaborator) {
        viewModelScope.launch {
            repository.toggleFavorite(collaborator)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshData()
        }
    }
}
