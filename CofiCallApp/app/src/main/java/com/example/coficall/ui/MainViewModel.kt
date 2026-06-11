package com.example.coficall.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coficall.data.DataRepository
import com.example.coficall.model.Collaborator
import com.example.coficall.model.BusinessUnit
import com.example.coficall.model.DepartmentInfo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DataRepository) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
    val currentUserEmail: StateFlow<String?> = repository.currentUserEmail
    val isOfflineMode: StateFlow<Boolean> = repository.isOfflineMode
    val isMockMode: StateFlow<Boolean> = repository.isMockMode
    val isInitializing: StateFlow<Boolean> = repository.isInitializing
    
    val collaborators: StateFlow<List<Collaborator>> = repository.collaborators
    val businessUnits = repository.businessUnits
    val language: StateFlow<String> = repository.language
    val updateInfo = kotlinx.coroutines.flow.MutableStateFlow<com.example.coficall.model.AppUpdateInfo?>(null)
    val serverVersionInfo = kotlinx.coroutines.flow.MutableStateFlow<com.example.coficall.model.AppUpdateInfo?>(null)

    var isDarkMode by mutableStateOf(false)
        private set

    var isSyncingContacts by mutableStateOf(false)
        private set

    var hasContactPermissionState by mutableStateOf(false)
        private set

    init {
        isDarkMode = repository.loadDarkMode()
        checkForUpdates()
        loadServerVersionInfo()
    }

    fun updateLanguage(lang: String) {
        repository.updateLanguage(lang)
    }

    // Business units com contagem reativa e real de acordo com as novas regras
    val businessUnitsWithCounts: StateFlow<List<BusinessUnit>> = combine(repository.businessUnits, repository.collaborators) { units, colabs ->
        units.map { unit ->
            val count = colabs.count { colab ->
                val bu = colab.businessUnit.replace(" ", "").uppercase()
                val dept = colab.department.replace(" ", "").uppercase()
                when (unit.shortName.replace(" ", "").uppercase()) {
                    "COFPT" -> bu == "COFPT" && dept != "R&D+I"
                    "COFGR" -> bu == "COFGR"
                    "COEPT" -> (bu == "COFPT" && dept == "R&D+I") || bu == "COEPT"
                    "COEGR" -> bu == "COEGR"
                    else -> bu == unit.shortName.replace(" ", "").uppercase()
                }
            }
            unit.copy(collaboratorCount = count)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Departamentos únicos gerados dinamicamente dos colaboradores
    val departments: StateFlow<List<DepartmentInfo>> = repository.collaborators.map { colabs ->
        colabs.filter { it.department.isNotEmpty() }
            .groupBy { it.department }
            .map { (dept, list) -> DepartmentInfo(dept, list.size) }
            .sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Filtros para o Diretório
    var filterSite by mutableStateOf<String?>(null)
        private set
    var filterDepartment by mutableStateOf<String?>(null)
        private set

    fun updateFilterSite(site: String?) {
        filterSite = site
        if (site != null) filterDepartment = null // Limpa o outro se ativo
    }

    fun updateFilterDepartment(dept: String?) {
        filterDepartment = dept
        if (dept != null) filterSite = null // Limpa o outro se ativo
    }

    fun clearFilters() {
        filterSite = null
        filterDepartment = null
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode = enabled
        repository.saveDarkMode(enabled)
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

    fun resetPassword(email: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = repository.resetPassword(email)
            onResult(res)
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            repository.checkForUpdates().onSuccess { info ->
                updateInfo.value = info
            }.onFailure {
                updateInfo.value = null
            }
        }
    }

    fun loadServerVersionInfo() {
        viewModelScope.launch {
            repository.getServerVersionInfo().onSuccess { info ->
                serverVersionInfo.value = info
            }
        }
    }

    fun promoteCurrentVersion(versionCode: Int, versionName: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = repository.promoteVersion(versionCode, versionName)
            if (res.isSuccess) {
                val current = serverVersionInfo.value
                if (current != null) {
                    serverVersionInfo.value = current.copy(
                        latestVersionCode = versionCode,
                        latestVersionName = versionName
                    )
                } else {
                    serverVersionInfo.value = com.example.coficall.model.AppUpdateInfo(
                        latestVersionCode = versionCode,
                        latestVersionName = versionName,
                        apkUrl = "",
                        forceUpdate = false
                    )
                }
            }
            onResult(res)
        }
    }

    fun updateApkUrl(url: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = repository.updateApkUrl(url)
            if (res.isSuccess) {
                val current = serverVersionInfo.value
                if (current != null) {
                    serverVersionInfo.value = current.copy(apkUrl = url)
                } else {
                    serverVersionInfo.value = com.example.coficall.model.AppUpdateInfo(
                        latestVersionCode = 0,
                        latestVersionName = "",
                        apkUrl = url,
                        forceUpdate = false
                    )
                }
            }
            onResult(res)
        }
    }

    fun syncContactsToPhone(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            isSyncingContacts = true
            val res = repository.syncContactsToPhone()
            isSyncingContacts = false
            onResult(res)
        }
    }

    fun clearContactsFromPhone(onResult: (Boolean) -> Unit) {
        repository.clearContactsFromPhone(onResult)
    }

    fun updateContactPermission(granted: Boolean) {
        hasContactPermissionState = granted
    }



    fun toggleFavorite(collaborator: Collaborator) {
        viewModelScope.launch {
            repository.toggleFavorite(collaborator)
        }
    }

    fun updateCollaboratorPhoto(collaboratorId: String, photoUrl: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.updateCollaboratorPhoto(collaboratorId, photoUrl)
            onResult(res)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshData()
        }
    }

    fun updateCollaboratorProfile(collaborator: Collaborator, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.updateCollaboratorProfile(collaborator)
            onResult(res)
        }
    }

    fun addCollaborator(collaborator: Collaborator, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.addCollaborator(collaborator)
            onResult(res)
        }
    }

    fun approveProfileUpdate(collaboratorId: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.approveProfileUpdate(collaboratorId)
            onResult(res)
        }
    }

    fun rejectProfileUpdate(collaboratorId: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.rejectProfileUpdate(collaboratorId)
            onResult(res)
        }
    }

    fun deleteCollaborator(collaboratorId: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.deleteCollaborator(collaboratorId)
            onResult(res)
        }
    }

    fun deleteCurrentUserAccount(onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.deleteCurrentUserAccount()
            onResult(res)
        }
    }

    fun repopulateDatabase(onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.repopulateDatabaseFromCsv()
            onResult(res)
        }
    }

    private val translations = mapOf(
        "PT" to mapOf(
            "home" to "Início",
            "collaborators" to "Colaboradores",
            "favorites" to "Favoritos",
            "no_favorites" to "Sem favoritos ainda",
            "add_favorites_hint" to "Toque na estrela ⭐ num colaborador\npara adicioná-lo aqui.",
            "settings" to "Definições",
            "factories" to "Fábricas",
            "coe" to "CoE",
            "departments" to "Departamentos",
            "collaborator" to "COLABORADOR",
            "collaborators_upper" to "COLABORADORES",
            "search_placeholder" to "Procurar Colaborador",
            "filters" to "Filtros",
            "offline_banner" to "A trabalhar offline. Última sincronização há 2m.",
            "my_account" to "Minha Conta",
            "offline_sync" to "Sincronização Offline",
            "dark_mode" to "Modo Escuro",
            "about" to "Sobre",
            "logout" to "Sair da Conta",
            "version" to "Versão 1.1.0 (Build 1.0.1)",
            "language" to "Idioma",
            "pt" to "🇵🇹 Português (PT)",
            "en" to "🇬🇧 Inglês (EN)",
            "fr" to "🇫🇷 Francês (FR)",
            "es" to "🇪🇸 Espanhol (ES)",

            "about_dialog_title" to "Sobre a Aplicação",
            "developed_by" to "Desenvolvido por Flávio Proença",
            "sync_dialog_title" to "Sincronização de Dados",
            "sync_now" to "Sincronizar Agora",
            "syncing" to "A sincronizar...",
            "sync_success" to "Dados sincronizados com sucesso!",
            "last_sync" to "Última sincronização: Hoje, 09:42",
            "cloud_db" to "Base de Dados Cloud",
            "local_db" to "Base de Dados Local",
            "synced" to "Sincronizado com Firebase",
            "updated" to "ATUALIZADO",
            "offline" to "OFFLINE",
            "edit_account_title" to "Editar Conta",
            "save" to "Guardar",
            "cancel" to "Cancelar",
            "name" to "Nome",
            "job_title" to "Cargo",
            "department_field" to "Departamento",
            "phone" to "Telefone",
            "extension" to "Extensão",
            "email" to "E-mail",
            "no_collaborators" to "Nenhum colaborador encontrado.",
            "business_units_title" to "FÁBRICAS DE PORTUGAL",
            "coes_title" to "CENTROS DE EXCELÊNCIA (CoE)",
            "admin_panel" to "Painel de Administração",
            "admin_desc" to "Estatísticas e controlo do sistema",
            "total_collaborators" to "Total Colaboradores",
            "online_collaborators" to "Online",
            "favorites_count" to "Favoritos",
            "reimport_db" to "Repovoar Base de Dados",
            "reimport_confirm" to "Tem a certeza que deseja repovoar a base de dados a partir do CSV?",
            "add_collaborator_title" to "Adicionar Colaborador",
            "pending_approvals" to "Aprovações Pendentes",
            "approve" to "Aprovar",
            "reject" to "Rejeitar",
            "proposed_value" to "Proposto",
            "current_value" to "Atual",
            "manage_collaborators" to "Gerir Todos os Colaboradores",
            "search_collaborators" to "Pesquisar colaborador..."
        ),
        "EN" to mapOf(
            "home" to "Home",
            "collaborators" to "Collaborators",
            "favorites" to "Favorites",
            "no_favorites" to "No favorites yet",
            "add_favorites_hint" to "Tap the star ⭐ on a collaborator\nto add them here.",
            "settings" to "Settings",
            "factories" to "Factories",
            "coe" to "CoE",
            "departments" to "Departments",
            "collaborator" to "COLLABORATOR",
            "collaborators_upper" to "COLLABORATORS",
            "search_placeholder" to "Search Collaborator",
            "filters" to "Filters",
            "offline_banner" to "Working offline. Last sync 2m ago.",
            "my_account" to "My Account",
            "offline_sync" to "Offline Sync",
            "dark_mode" to "Dark Mode",
            "about" to "About",
            "logout" to "Log Out",
            "version" to "Version 1.1.0 (Build 1.0.1)",
            "language" to "Language",
            "pt" to "🇵🇹 Portuguese (PT)",
            "en" to "🇬🇧 English (EN)",
            "fr" to "🇫🇷 French (FR)",
            "es" to "🇪🇸 Spanish (ES)",

            "about_dialog_title" to "About the App",
            "developed_by" to "Developed by Flávio Proença",
            "sync_dialog_title" to "Data Synchronization",
            "sync_now" to "Sync Now",
            "syncing" to "Syncing...",
            "sync_success" to "Data synchronized successfully!",
            "last_sync" to "Last sync: Today, 09:42",
            "cloud_db" to "Cloud Database",
            "local_db" to "Local Database",
            "synced" to "Synced with Firebase",
            "updated" to "UPDATED",
            "offline" to "OFFLINE",
            "edit_account_title" to "Edit Account",
            "save" to "Save",
            "cancel" to "Cancel",
            "name" to "Name",
            "job_title" to "Job Title",
            "department_field" to "Department",
            "phone" to "Phone",
            "extension" to "Extension",
            "email" to "Email",
            "no_collaborators" to "No collaborators found.",
            "business_units_title" to "PORTUGAL FACTORIES",
            "coes_title" to "CENTERS OF EXCELLENCE (CoE)",
            "admin_panel" to "Admin Panel",
            "admin_desc" to "System control and statistics",
            "total_collaborators" to "Total Collaborators",
            "online_collaborators" to "Online",
            "favorites_count" to "Favorites",
            "reimport_db" to "Repopulate Database",
            "reimport_confirm" to "Are you sure you want to repopulate the database from CSV?",
            "add_collaborator_title" to "Add Collaborator",
            "pending_approvals" to "Pending Approvals",
            "approve" to "Approve",
            "reject" to "Reject",
            "proposed_value" to "Proposed",
            "current_value" to "Current",
            "manage_collaborators" to "Manage All Collaborators",
            "search_collaborators" to "Search collaborator..."
        ),
        "FR" to mapOf(
            "home" to "Accueil",
            "collaborators" to "Collaborateurs",
            "favorites" to "Favoris",
            "no_favorites" to "Pas encore de favoris",
            "add_favorites_hint" to "Appuyez sur l'étoile ⭐ sur un collaborateur\npour l'ajouter ici.",
            "settings" to "Paramètres",
            "factories" to "Usines",
            "coe" to "CoE",
            "departments" to "Départements",
            "collaborator" to "COLLABORATEUR",
            "collaborators_upper" to "COLLABORATEURS",
            "search_placeholder" to "Rechercher Collaborateur",
            "filters" to "Filtres",
            "offline_banner" to "Hors ligne. Dernière sync il y a 2m.",
            "my_account" to "Mon Compte",
            "offline_sync" to "Sync Hors ligne",
            "dark_mode" to "Mode Sombre",
            "about" to "À propos",
            "logout" to "Se déconnecter",
            "version" to "Version 1.1.0 (Build 1.0.1)",
            "language" to "Langue",
            "pt" to "🇵🇹 Portugais (PT)",
            "en" to "🇬🇧 Anglais (EN)",
            "fr" to "🇫🇷 Français (FR)",
            "es" to "🇪🇸 Espagnol (ES)",

            "about_dialog_title" to "À propos de l'application",
            "developed_by" to "Développé par Flávio Proença",
            "sync_dialog_title" to "Synchronisation de données",
            "sync_now" to "Sync maintenant",
            "syncing" to "Synchronisation...",
            "sync_success" to "Données synchronisées!",
            "last_sync" to "Dernière sync: Aujourd'hui, 09:42",
            "cloud_db" to "Base de données cloud",
            "local_db" to "Base de données locale",
            "synced" to "Synchronisé avec Firebase",
            "updated" to "MIS À JOUR",
            "offline" to "HORS LIGNE",
            "edit_account_title" to "Modifier le compte",
            "save" to "Enregistrer",
            "cancel" to "Annuler",
            "name" to "Nom",
            "job_title" to "Poste",
            "department_field" to "Département",
            "phone" to "Téléphone",
            "extension" to "Extension",
            "email" to "Email",
            "no_collaborators" to "Aucun collaborateur trouvé.",
            "business_units_title" to "USINES DE PORTUGAL",
            "coes_title" to "CENTRES D'EXCELLENCE (CoE)",
            "admin_panel" to "Panneau d'administration",
            "admin_desc" to "Contrôle et statistiques du système",
            "total_collaborators" to "Total Collaborateurs",
            "online_collaborators" to "En ligne",
            "favorites_count" to "Favoris",
            "reimport_db" to "Repeupler Base de Données",
            "reimport_confirm" to "Êtes-vous sûr de vouloir repeupler la base de données à partir du CSV?",
            "add_collaborator_title" to "Ajouter un Collaborateur",
            "pending_approvals" to "Approbations en attente",
            "approve" to "Approuver",
            "reject" to "Rejeter",
            "proposed_value" to "Proposé",
            "current_value" to "Actuel",
            "manage_collaborators" to "Gérer tous les collaborateurs",
            "search_collaborators" to "Rechercher un collaborateur..."
        ),
        "ES" to mapOf(
            "home" to "Inicio",
            "collaborators" to "Colaboradores",
            "favorites" to "Favoritos",
            "no_favorites" to "Sin favoritos todavía",
            "add_favorites_hint" to "Toca la estrella ⭐ en un colaborador\npara agregarlo aquí.",
            "settings" to "Definiciones",
            "factories" to "Fábricas",
            "coe" to "CoE",
            "departments" to "Departamentos",
            "collaborator" to "COLABORADOR",
            "collaborators_upper" to "COLABORADORES",
            "search_placeholder" to "Buscar Colaborador",
            "filters" to "Filtros",
            "offline_banner" to "Trabajando sin conexión. Última sinc hace 2m.",
            "my_account" to "Mi Cuenta",
            "offline_sync" to "Sincronización Offline",
            "dark_mode" to "Modo Oscuro",
            "about" to "Acerca de",
            "logout" to "Cerrar Sesión",
            "version" to "Versión 1.1.0 (Build 1.0.1)",
            "language" to "Idioma",
            "pt" to "🇵🇹 Portugués (PT)",
            "en" to "🇬🇧 Inglés (EN)",
            "fr" to "🇫🇷 Francés (FR)",
            "es" to "🇪🇸 Español (ES)",

            "about_dialog_title" to "Acerca de la app",
            "developed_by" to "Desarrollado por Flávio Proença",
            "sync_dialog_title" to "Sincronización de datos",
            "sync_now" to "Sincronizar ahora",
            "syncing" to "Sincronizando...",
            "sync_success" to "¡Datos sincronizados con éxito!",
            "last_sync" to "Última sinc: Hoy, 09:42",
            "cloud_db" to "Base de datos Cloud",
            "local_db" to "Base de datos local",
            "synced" to "Sincronizado con Firebase",
            "updated" to "ACTUALIZADO",
            "offline" to "SIN CONEXIÓN",
            "edit_account_title" to "Editar Cuenta",
            "save" to "Guardar",
            "cancel" to "Cancelar",
            "name" to "Nombre",
            "job_title" to "Cargo",
            "department_field" to "Departamento",
            "phone" to "Teléfono",
            "extension" to "Extensión",
            "email" to "Correo electrónico",
            "no_collaborators" to "No se encontraron colaboradores.",
            "business_units_title" to "FÁBRICAS DE PORTUGAL",
            "coes_title" to "CENTROS DE EXCELENCIA (CoE)",
            "admin_panel" to "Panel de Administración",
            "admin_desc" to "Estadísticas y control del sistema",
            "total_collaborators" to "Total Colaboradores",
            "online_collaborators" to "En línea",
            "favorites_count" to "Favoritos",
            "reimport_db" to "Repoblar Base de Datos",
            "reimport_confirm" to "¿Está seguro de que desea repoblar la datos desde el CSV?",
            "add_collaborator_title" to "Agregar Colaborador",
            "pending_approvals" to "Aprobaciones Pendientes",
            "approve" to "Aprobar",
            "reject" to "Rechazar",
            "proposed_value" to "Propuesto",
            "current_value" to "Actual",
            "manage_collaborators" to "Gestionar Todos los Colaboradores",
            "search_collaborators" to "Buscar colaborador..."
        )
    )

    fun getString(key: String): String {
        val lang = language.value
        return translations[lang]?.get(key) ?: translations["PT"]?.get(key) ?: key
    }
}
