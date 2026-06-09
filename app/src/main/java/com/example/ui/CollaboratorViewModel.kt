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
    private val prefs = application.getSharedPreferences("coficall_prefs", android.content.Context.MODE_PRIVATE)

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

    // Local language selection
    var appLanguage by mutableStateOf(prefs.getString("app_language", "pt") ?: "pt")
        private set

    fun setLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        appLanguage = lang
    }

    // Local authentication & profile state
    var isLoggedIn by mutableStateOf(prefs.getBoolean("is_logged_in", false))
        private set

    var currentUserEmail by mutableStateOf(prefs.getString("current_user_email", "") ?: "")
        private set

    var currentUserName by mutableStateOf(prefs.getString("current_user_name", "") ?: "")
        private set

    var currentUserDept by mutableStateOf(prefs.getString("current_user_dept", "") ?: "")
        private set

    var currentUserCompany by mutableStateOf(prefs.getString("current_user_company", "") ?: "")
        private set

    var currentUserPhone by mutableStateOf(prefs.getString("current_user_phone", "") ?: "")
        private set

    var currentUserPhoto by mutableStateOf(prefs.getString("current_user_photo", "") ?: "")
        private set

    val isAdmin: Boolean
        get() = currentUserEmail.equals("flavio.proenca@coficab.com", ignoreCase = true)

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

    // Auth actions
    fun registerUser(name: String, email: String, password: String, dept: String, comp: String, phone: String) {
        val emailKey = email.lowercase().trim()
        prefs.edit().apply {
            putString("reg_email_$emailKey", email.trim())
            putString("reg_password_$emailKey", password)
            putString("reg_name_$emailKey", name)
            putString("reg_dept_$emailKey", dept)
            putString("reg_company_$emailKey", comp)
            putString("reg_phone_$emailKey", phone)
            putString("reg_photo_$emailKey", "")
            apply()
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        val emailKey = email.lowercase().trim()
        val savedEmail = prefs.getString("reg_email_$emailKey", null)
        val savedPassword = prefs.getString("reg_password_$emailKey", null)

        val isFlavioAdmin = emailKey == "flavio.proenca@coficab.com"
        val isPassCorrect = (savedEmail != null && savedPassword == password) || (isFlavioAdmin && password == "admin123")

        if (isPassCorrect) {
            val name = prefs.getString("reg_name_$emailKey", if (isFlavioAdmin) "Flávio Proença" else "Ricardo Silva") ?: "Ricardo Silva"
            val dept = prefs.getString("reg_dept_$emailKey", if (isFlavioAdmin) "Administrador de Sistemas" else "Diretor de Operações") ?: "Diretor de Operações"
            val comp = prefs.getString("reg_company_$emailKey", if (isFlavioAdmin) "COF PT" else "LOGÍSTICA GLOBAL S.A.") ?: "LOGÍSTICA GLOBAL S.A."
            val phone = prefs.getString("reg_phone_$emailKey", if (isFlavioAdmin) "+351 910 352 747" else "+351 912 345 600") ?: "+351 912 345 600"
            val photo = prefs.getString("reg_photo_$emailKey", "") ?: ""

            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("current_user_email", email)
                putString("current_user_name", name)
                putString("current_user_dept", dept)
                putString("current_user_company", comp)
                putString("current_user_phone", phone)
                putString("current_user_photo", photo)
                apply()
            }

            isLoggedIn = true
            currentUserEmail = email
            currentUserName = name
            currentUserDept = dept
            currentUserCompany = comp
            currentUserPhone = phone
            currentUserPhoto = photo
            onResult(true)
        } else {
            onResult(false)
        }
    }

    fun logoutUser() {
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            apply()
        }
        isLoggedIn = false
    }

    fun updateCurrentUserProfile(name: String, dept: String, comp: String, phone: String, photo: String) {
        val emailKey = currentUserEmail.lowercase().trim()
        prefs.edit().apply {
            putString("current_user_name", name)
            putString("current_user_dept", dept)
            putString("current_user_company", comp)
            putString("current_user_phone", phone)
            putString("current_user_photo", photo)
            
            putString("reg_name_$emailKey", name)
            putString("reg_dept_$emailKey", dept)
            putString("reg_company_$emailKey", comp)
            putString("reg_phone_$emailKey", phone)
            putString("reg_photo_$emailKey", photo)
            apply()
        }
        currentUserName = name
        currentUserDept = dept
        currentUserCompany = comp
        currentUserPhone = phone
        currentUserPhoto = photo
    }

    // CRUD actions
    fun addCollaborator(
        name: String,
        department: String,
        company: String,
        email: String,
        phone: String,
        photoUrl: String,
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
                photoUrl = photoUrl,
                isFavorite = false,
                status = "online",
                isFactory = isFactory,
                isOffice = isOffice
            )
            repository.insert(newC)
            isAddDialogOpen = false
        }
    }

    fun deleteCollaborator(collaborator: Collaborator) {
        viewModelScope.launch {
            repository.delete(collaborator)
        }
    }

    fun updateCollaborator(collaborator: Collaborator) {
        viewModelScope.launch {
            repository.update(collaborator)
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
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
