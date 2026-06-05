package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Collaborator
import com.example.data.CollaboratorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CollaboratorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CollaboratorRepository

    // Native support for dark theme saved in memory
    var isDarkTheme by mutableStateOf(false)
        private set

    // Native screen navigation
    var currentScreen by mutableStateOf(Screen.Inicio)
        private set

    // Filter selection on main screen
    var mainFilter by mutableStateOf(MainFilter.Unidades)
        private set

    // Filters on directory screen
    var searchQuery by mutableStateOf("")
        private set

    var selectedLetter by mutableStateOf<String?>(null)
        private set

    var selectedCompanyFilter by mutableStateOf<String?>(null)
        private set

    // Dialog state
    var isAddDialogOpen by mutableStateOf(false)
        private set

    // Last sync time string
    private val _lastSyncTime = MutableStateFlow("Hoje, 09:42")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val db = AppDatabase.getDatabase(application)
    
    val allCollaborators: StateFlow<List<Collaborator>>

    init {
        repository = CollaboratorRepository(db.collaboratorDao())
        
        // Expose database flow
        allCollaborators = repository.allCollaborators
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Prepopulate database if absolutely empty
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkTheme = enabled
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        // Reset local filters when changing screens except if specifically passed
        if (screen != Screen.Colaboradores) {
            // keep it clean
        }
    }

    fun setMainTabFilter(filter: MainFilter) {
        mainFilter = filter
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun selectLetterFilter(letter: String?) {
        selectedLetter = if (selectedLetter == letter) null else letter
    }

    fun filterByCompany(company: String?) {
        selectedCompanyFilter = company
    }

    fun toggleFavorite(collaborator: Collaborator) {
        viewModelScope.launch {
            repository.update(collaborator.copy(isFavorite = !collaborator.isFavorite))
        }
    }

    fun addCollaborator(
        name: String,
        department: String,
        company: String,
        email: String,
        phone: String,
        isFactory: Boolean,
        isOffice: Boolean
    ) {
        viewModelScope.launch {
            val newC = Collaborator(
                name = name,
                department = department,
                company = company,
                email = email,
                phone = phone,
                photoUrl = "", // default placeholder initials
                isFavorite = false,
                status = "online",
                isFactory = isFactory,
                isOffice = isOffice
            )
            repository.insert(newC)
            isAddDialogOpen = false
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            // Update timestamp
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            _lastSyncTime.value = "Hoje, " + sdf.format(Date())
        }
    }

    fun openAddDialog() {
        isAddDialogOpen = true
    }

    fun closeAddDialog() {
        isAddDialogOpen = false
    }
}

enum class Screen {
    Inicio, Colaboradores, Favoritos, Definições
}

enum class MainFilter {
    Unidades, Fabricas, Escritorios
}
