package com.example.coficall.data

import android.content.Context
import android.util.Log
import com.example.coficall.model.BusinessUnit
import com.example.coficall.model.Collaborator
import com.example.coficall.model.sampleBusinessUnits
import com.example.coficall.model.sampleCollaborators
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.text.Normalizer

interface DataRepository {
    val isLoggedIn: StateFlow<Boolean>
    val currentUserEmail: StateFlow<String?>
    val isOfflineMode: StateFlow<Boolean>
    val isMockMode: StateFlow<Boolean>
    val isInitializing: StateFlow<Boolean>
    
    val collaborators: StateFlow<List<Collaborator>>
    val businessUnits: StateFlow<List<BusinessUnit>>
    val language: StateFlow<String>
    
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun logout()
    suspend fun toggleFavorite(collaborator: Collaborator)
    suspend fun refreshData()
    suspend fun updateCollaboratorPhoto(collaboratorId: String, photoUrl: String): Result<Unit>
    suspend fun updateCollaboratorProfile(collaborator: Collaborator): Result<Unit>
    fun updateLanguage(lang: String)
    suspend fun addCollaborator(collaborator: Collaborator): Result<Unit>
    suspend fun repopulateDatabaseFromCsv(): Result<Unit>
    suspend fun approveProfileUpdate(collaboratorId: String): Result<Unit>
    suspend fun rejectProfileUpdate(collaboratorId: String): Result<Unit>
    suspend fun deleteCollaborator(collaboratorId: String): Result<Unit>
    suspend fun deleteCurrentUserAccount(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun checkForUpdates(): Result<com.example.coficall.model.AppUpdateInfo?>
    suspend fun promoteVersion(versionCode: Int, versionName: String): Result<Unit>
    suspend fun updateApkUrl(url: String): Result<Unit>
    suspend fun getServerVersionInfo(): Result<com.example.coficall.model.AppUpdateInfo?>
    fun saveDarkMode(enabled: Boolean)
    fun loadDarkMode(): Boolean
}


class DefaultDataRepository(private val context: Context) : DataRepository {
    private val prefs = context.getSharedPreferences("coficall_prefs", Context.MODE_PRIVATE)
    
    private var isImportingCollaborators = false
    private var isImportingBusinessUnits = false
    
    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isInitializing = MutableStateFlow(true)
    override val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    override val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    override val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _isMockMode = MutableStateFlow(false)
    override val isMockMode: StateFlow<Boolean> = _isMockMode.asStateFlow()

    private val _collaborators = MutableStateFlow<List<Collaborator>>(emptyList())
    override val collaborators: StateFlow<List<Collaborator>> = _collaborators.asStateFlow()

    private val _businessUnits = MutableStateFlow<List<BusinessUnit>>(emptyList())
    override val businessUnits: StateFlow<List<BusinessUnit>> = _businessUnits.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString("language", "PT") ?: "PT")
    override val language: StateFlow<String> = _language.asStateFlow()

    private var collaboratorsRegistration: ListenerRegistration? = null
    private var businessUnitsRegistration: ListenerRegistration? = null

    private val favoriteIds: MutableSet<String>
        get() = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()

    init {
        Log.d("CofiCallDebug", "DefaultDataRepository: init iniciado")
        checkFirebaseAvailability()
    }

    private fun checkFirebaseAvailability() {
        Log.d("CofiCallDebug", "DefaultDataRepository: checkFirebaseAvailability")
        val isFirebaseAvailable = try {
            FirebaseApp.getInstance()
            Log.d("CofiCallDebug", "DefaultDataRepository: FirebaseApp está disponível")
            true
        } catch (e: Exception) {
            Log.d("CofiCallDebug", "DefaultDataRepository: FirebaseApp não está disponível (Modo Mock)")
            false
        }

        if (isFirebaseAvailable) {
            _isMockMode.value = false
            val auth = FirebaseAuth.getInstance()
            
            // Forçar logout na inicialização para exigir sempre login/registo
            try {
                auth.signOut()
            } catch (e: Exception) {
                Log.e("CofiCallDebug", "Erro ao fazer signOut na inicialização", e)
            }
            
            _isLoggedIn.value = false
            _currentUserEmail.value = null
            _isInitializing.value = false
            
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                Log.d("CofiCallDebug", "DefaultDataRepository: AuthStateListener disparou. User: ${user?.email}")
                if (user != null) {
                    _isLoggedIn.value = true
                    _currentUserEmail.value = user.email
                    startFirebaseSync()
                } else {
                    _isLoggedIn.value = false
                    _currentUserEmail.value = null
                    _isInitializing.value = false
                    stopFirebaseSync()
                    _collaborators.value = emptyList()
                    _businessUnits.value = emptyList()
                }
            }
        } else {
            _isMockMode.value = true
            // Limpar estado logado do Mock ao abrir a app
            prefs.edit()
                .putBoolean("mock_logged_in", false)
                .putString("mock_email", null)
                .apply()
            
            _isLoggedIn.value = false
            _currentUserEmail.value = null
            loadMockData()
        }
    }

    private fun loadMockData() {
        Log.d("CofiCallDebug", "DefaultDataRepository: loadMockData iniciado")
        val favs = favoriteIds
        val mappedCollaborators = sampleCollaborators.map {
            it.copy(isFavorite = favs.contains(it.id))
        }
        _collaborators.value = mappedCollaborators
        _businessUnits.value = sampleBusinessUnits
        _isOfflineMode.value = false
        _isInitializing.value = false
        Log.d("CofiCallDebug", "DefaultDataRepository: loadMockData concluído. Colabs: ${mappedCollaborators.size}")
    }

    private fun startFirebaseSync() {
        Log.d("CofiCallDebug", "DefaultDataRepository: startFirebaseSync")
        stopFirebaseSync()

        val db = FirebaseFirestore.getInstance()
        
        collaboratorsRegistration = db.collection("collaborators")
            .addSnapshotListener { snapshot, error ->
                Log.d("CofiCallDebug", "DefaultDataRepository: collaborators snapshot listener disparou. Error: ${error?.message}, Empty: ${snapshot?.isEmpty}")
                if (error != null) {
                    Log.e("DefaultDataRepository", "Error syncing collaborators", error)
                    _isOfflineMode.value = true
                    _isInitializing.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val attemptedImport = prefs.getBoolean("attempted_import", false)
                    if (snapshot.isEmpty && !isImportingCollaborators && !snapshot.metadata.isFromCache && !attemptedImport) {
                        Log.d("DefaultDataRepository", "Firestore de colaboradores vazio! A iniciar importacao automatica...")
                        importContactsFromCsv()
                    }
                    val favs = favoriteIds
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: ""
                            val jobTitle = doc.getString("jobTitle") ?: ""
                            val department = doc.getString("department") ?: ""
                            val businessUnit = doc.getString("businessUnit") ?: ""
                            val phone = doc.getString("phone")
                            val extension = doc.getString("extension")
                            val email = doc.getString("email")
                            val photoUrl = doc.getString("photoUrl")
                            val isOnline = doc.getBoolean("isOnline") ?: false
                            
                            val pendingName = doc.getString("pendingName")
                            val pendingJobTitle = doc.getString("pendingJobTitle")
                            val pendingDepartment = doc.getString("pendingDepartment")
                            val pendingPhone = doc.getString("pendingPhone")
                            val pendingExtension = doc.getString("pendingExtension")
                            val pendingEmail = doc.getString("pendingEmail")
                            val hasPendingChanges = doc.getBoolean("hasPendingChanges") ?: false

                            Collaborator(
                                id = id,
                                name = name,
                                jobTitle = jobTitle,
                                department = department,
                                businessUnit = businessUnit,
                                phone = phone,
                                extension = extension,
                                email = email,
                                photoUrl = photoUrl,
                                isFavorite = favs.contains(id),
                                isOnline = isOnline,
                                pendingName = pendingName,
                                pendingJobTitle = pendingJobTitle,
                                pendingDepartment = pendingDepartment,
                                pendingPhone = pendingPhone,
                                pendingExtension = pendingExtension,
                                pendingEmail = pendingEmail,
                                hasPendingChanges = hasPendingChanges
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    Log.d("CofiCallDebug", "DefaultDataRepository: collaborators mapeados: ${list.size}")
                    _collaborators.value = list
                    _isOfflineMode.value = snapshot.metadata.isFromCache
                    _isInitializing.value = false
                }
            }

        businessUnitsRegistration = db.collection("businessUnits")
            .addSnapshotListener { snapshot, error ->
                Log.d("CofiCallDebug", "DefaultDataRepository: businessUnits snapshot listener disparou. Error: ${error?.message}, Empty: ${snapshot?.isEmpty}")
                if (error != null) {
                    Log.e("DefaultDataRepository", "Error syncing business units", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val attemptedBuImport = prefs.getBoolean("attempted_bu_import", false)
                    if (snapshot.isEmpty && !isImportingBusinessUnits && !snapshot.metadata.isFromCache && !attemptedBuImport) {
                        Log.d("DefaultDataRepository", "Firestore de businessUnits vazio! A iniciar preenchimento...")
                        importBusinessUnits()
                    }
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: ""
                            val shortName = doc.getString("shortName") ?: ""
                            val country = doc.getString("country") ?: ""
                            val count = doc.getLong("collaboratorCount")?.toInt() ?: 0
                            val typeStr = doc.getString("type") ?: "UNIT"
                            val type = try {
                                com.example.coficall.model.BusinessUnitType.valueOf(typeStr)
                            } catch (e: Exception) {
                                com.example.coficall.model.BusinessUnitType.UNIT
                            }

                            BusinessUnit(
                                id = id,
                                name = if (shortName == "COF GR") "Portugal" else name,
                                shortName = shortName,
                                country = if (shortName == "COF GR") "Portugal" else country,
                                collaboratorCount = count,
                                type = type
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    Log.d("CofiCallDebug", "DefaultDataRepository: businessUnits mapeados: ${list.size}")
                    _businessUnits.value = list
                }
            }
    }

    private fun generateEmailFromName(name: String): String {
        val clean = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .replace("[^a-z0-9\\s]".toRegex(), "")
            .trim()
        val parts = clean.split("\\s+".toRegex())
        if (parts.isEmpty()) return "colaborador@coficab.com"
        val first = parts.first()
        val last = if (parts.size > 1) parts.last() else ""
        return if (last.isEmpty()) "$first@coficab.com" else "$first.$last@coficab.com"
    }

    private fun importContactsFromCsv() {
        if (isImportingCollaborators) return
        isImportingCollaborators = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val inputStream = context.assets.open("contactos.csv")
                val reader = inputStream.bufferedReader()
                val lines = reader.readLines()
                if (lines.size <= 1) {
                    isImportingCollaborators = false
                    return@launch
                }
                
                val batch = db.batch()
                
                for (i in 1 until lines.size) {
                    val line = lines[i].trim()
                    if (line.isEmpty()) continue
                    val parts = line.split(";")
                    if (parts.size < 5) continue
                    
                    val name = parts[0].trim()
                    val extension = parts[1].trim()
                    val phone = parts[2].trim()
                    val department = parts[3].trim()
                    val site = parts[4].trim()
                    
                    val email = generateEmailFromName(name)
                    val docId = email.replace("@coficab.com", "").replace(".", "_")
                    
                    val colab = Collaborator(
                        id = docId,
                        name = name,
                        jobTitle = department,
                        department = department,
                        businessUnit = site.uppercase(),
                        phone = if (phone == "0" || phone.isEmpty() || phone == "null") null else phone,
                        extension = if (extension == "0" || extension.isEmpty() || extension == "null") null else extension,
                        email = email,
                        photoUrl = null,
                        isFavorite = false,
                        isOnline = (i % 7 == 0)
                    )
                    
                    val data = hashMapOf(
                        "id" to colab.id,
                        "name" to colab.name,
                        "jobTitle" to colab.jobTitle,
                        "department" to colab.department,
                        "businessUnit" to colab.businessUnit,
                        "phone" to colab.phone,
                        "extension" to colab.extension,
                        "email" to colab.email,
                        "photoUrl" to colab.photoUrl,
                        "isOnline" to colab.isOnline
                    )
                    
                    val docRef = db.collection("collaborators").document(docId)
                    batch.set(docRef, data)
                }
                
                batch.commit().addOnSuccessListener {
                    Log.d("DefaultDataRepository", "Importacao de contactos concluida com sucesso!")
                    prefs.edit().putBoolean("attempted_import", true).apply()
                    isImportingCollaborators = false
                }.addOnFailureListener { e ->
                    Log.e("DefaultDataRepository", "Erro ao importar contactos", e)
                    prefs.edit().putBoolean("attempted_import", true).apply()
                    isImportingCollaborators = false
                }
            } catch (e: Exception) {
                Log.e("DefaultDataRepository", "Erro na leitura do CSV", e)
                isImportingCollaborators = false
            }
        }
    }

    private fun importBusinessUnits() {
        if (isImportingBusinessUnits) return
        isImportingBusinessUnits = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()
                for (bu in sampleBusinessUnits) {
                    val data = hashMapOf(
                        "id" to bu.id,
                        "name" to bu.name,
                        "shortName" to bu.shortName,
                        "country" to bu.country,
                        "collaboratorCount" to bu.collaboratorCount,
                        "type" to bu.type.name
                    )
                    val docRef = db.collection("businessUnits").document(bu.id)
                    batch.set(docRef, data)
                }
                batch.commit().addOnSuccessListener {
                    Log.d("DefaultDataRepository", "Unidades de negocio inseridas!")
                    prefs.edit().putBoolean("attempted_bu_import", true).apply()
                    isImportingBusinessUnits = false
                }.addOnFailureListener { e ->
                    Log.e("DefaultDataRepository", "Erro ao inserir unidades", e)
                    prefs.edit().putBoolean("attempted_bu_import", true).apply()
                    isImportingBusinessUnits = false
                }
            } catch (e: Exception) {
                Log.e("DefaultDataRepository", "Erro ao preencher unidades", e)
                isImportingBusinessUnits = false
            }
        }
    }

    private fun stopFirebaseSync() {
        collaboratorsRegistration?.remove()
        collaboratorsRegistration = null
        businessUnitsRegistration?.remove()
        businessUnitsRegistration = null
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        if (!email.endsWith("@coficab.com")) {
            return Result.failure(IllegalArgumentException("Apenas o domínio @coficab.com é permitido."))
        }

        return try {
            if (_isMockMode.value) {
                prefs.edit()
                    .putBoolean("mock_logged_in", true)
                    .putString("mock_email", email)
                    .apply()
                _isLoggedIn.value = true
                _currentUserEmail.value = email
                loadMockData()
                Result.success(Unit)
            } else {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        if (!email.endsWith("@coficab.com")) {
            return Result.failure(IllegalArgumentException("Apenas o domínio @coficab.com é permitido."))
        }

        return try {
            if (_isMockMode.value) {
                prefs.edit()
                    .putBoolean("mock_logged_in", true)
                    .putString("mock_email", email)
                    .apply()
                _isLoggedIn.value = true
                _currentUserEmail.value = email
                loadMockData()
                Result.success(Unit)
            } else {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        if (_isMockMode.value) {
            prefs.edit()
                .putBoolean("mock_logged_in", false)
                .remove("mock_email")
                .apply()
            _isLoggedIn.value = false
            _currentUserEmail.value = null
            _collaborators.value = emptyList()
            _businessUnits.value = emptyList()
        } else {
            FirebaseAuth.getInstance().signOut()
        }
    }

    override suspend fun toggleFavorite(collaborator: Collaborator) {
        val favs = favoriteIds
        val id = collaborator.id
        if (favs.contains(id)) {
            favs.remove(id)
        } else {
            favs.add(id)
        }
        
        // Save back to prefs
        prefs.edit().putStringSet("favorites", favs).apply()

        // Update state
        _collaborators.value = _collaborators.value.map {
            if (it.id == id) {
                it.copy(isFavorite = favs.contains(id))
            } else {
                it
            }
        }
    }

    override suspend fun refreshData() {
        if (_isMockMode.value) {
            loadMockData()
        } else {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("collaborators")
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .await()
                db.collection("businessUnits")
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .await()
                _isOfflineMode.value = false
                Log.d("CofiCallDebug", "refreshData: Sincronizacao com Firebase bem-sucedida do servidor.")
            } catch (e: Exception) {
                Log.e("CofiCallDebug", "refreshData: Falha na sincronizacao online (offline)", e)
                _isOfflineMode.value = true
            }
        }
    }

    override suspend fun updateCollaboratorPhoto(collaboratorId: String, photoUrl: String): Result<Unit> {
        return try {
            if (_isMockMode.value) {
                _collaborators.value = _collaborators.value.map {
                    if (it.id == collaboratorId) it.copy(photoUrl = photoUrl) else it
                }
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                db.collection("collaborators")
                    .document(collaboratorId)
                    .update("photoUrl", photoUrl)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCollaboratorProfile(collaborator: Collaborator): Result<Unit> {
        return try {
            val isAdmin = _currentUserEmail.value == "flavio.proenca@coficab.com"
            if (_isMockMode.value) {
                _collaborators.value = _collaborators.value.map {
                    if (it.id == collaborator.id) {
                        if (isAdmin) {
                            collaborator.copy(
                                pendingName = null, pendingJobTitle = null, pendingDepartment = null,
                                pendingPhone = null, pendingExtension = null, pendingEmail = null,
                                hasPendingChanges = false
                            )
                        } else {
                            it.copy(
                                pendingName = collaborator.name,
                                pendingJobTitle = collaborator.jobTitle,
                                pendingDepartment = collaborator.department,
                                pendingPhone = collaborator.phone,
                                pendingExtension = collaborator.extension,
                                pendingEmail = collaborator.email,
                                hasPendingChanges = true
                            )
                        }
                    } else it
                }
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                val data = if (isAdmin) {
                    hashMapOf(
                        "name" to collaborator.name,
                        "jobTitle" to collaborator.jobTitle,
                        "department" to collaborator.department,
                        "phone" to collaborator.phone,
                        "extension" to collaborator.extension,
                        "email" to collaborator.email,
                        "pendingName" to null,
                        "pendingJobTitle" to null,
                        "pendingDepartment" to null,
                        "pendingPhone" to null,
                        "pendingExtension" to null,
                        "pendingEmail" to null,
                        "hasPendingChanges" to false
                    )
                } else {
                    hashMapOf(
                        "pendingName" to collaborator.name,
                        "pendingJobTitle" to collaborator.jobTitle,
                        "pendingDepartment" to collaborator.department,
                        "pendingPhone" to collaborator.phone,
                        "pendingExtension" to collaborator.extension,
                        "pendingEmail" to collaborator.email,
                        "hasPendingChanges" to true
                    )
                }
                db.collection("collaborators")
                    .document(collaborator.id)
                    .update(data as Map<String, Any>)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun updateLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _language.value = lang
    }

    override suspend fun addCollaborator(collaborator: Collaborator): Result<Unit> {
        return try {
            if (_isMockMode.value) {
                val newList = _collaborators.value.toMutableList()
                newList.add(collaborator)
                _collaborators.value = newList
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "id" to collaborator.id,
                    "name" to collaborator.name,
                    "jobTitle" to collaborator.jobTitle,
                    "department" to collaborator.department,
                    "businessUnit" to collaborator.businessUnit,
                    "phone" to collaborator.phone,
                    "extension" to collaborator.extension,
                    "email" to collaborator.email,
                    "photoUrl" to collaborator.photoUrl,
                    "isOnline" to collaborator.isOnline,
                    "hasPendingChanges" to false
                )
                db.collection("collaborators")
                    .document(collaborator.id)
                    .set(data)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun repopulateDatabaseFromCsv(): Result<Unit> {
        return try {
            if (_isMockMode.value) {
                loadMockData()
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                val colabs = db.collection("collaborators").get().await()
                val batch = db.batch()
                for (doc in colabs.documents) {
                    batch.delete(doc.reference)
                }
                val bus = db.collection("businessUnits").get().await()
                for (doc in bus.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                
                isImportingCollaborators = false
                isImportingBusinessUnits = false
                prefs.edit()
                    .putBoolean("attempted_import", false)
                    .putBoolean("attempted_bu_import", false)
                    .apply()
                importContactsFromCsv()
                importBusinessUnits()
                
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveProfileUpdate(collaboratorId: String): Result<Unit> {
        return try {
            if (_isMockMode.value) {
                _collaborators.value = _collaborators.value.map {
                    if (it.id == collaboratorId && it.hasPendingChanges) {
                        it.copy(
                            name = it.pendingName ?: it.name,
                            jobTitle = it.pendingJobTitle ?: it.jobTitle,
                            department = it.pendingDepartment ?: it.department,
                            phone = it.pendingPhone ?: it.phone,
                            extension = it.pendingExtension ?: it.extension,
                            email = it.pendingEmail ?: it.email,
                            pendingName = null, pendingJobTitle = null, pendingDepartment = null,
                            pendingPhone = null, pendingExtension = null, pendingEmail = null,
                            hasPendingChanges = false
                        )
                    } else it
                }
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("collaborators").document(collaboratorId).get().await()
                if (doc.exists()) {
                    val pName = doc.getString("pendingName") ?: doc.getString("name") ?: ""
                    val pJob = doc.getString("pendingJobTitle") ?: doc.getString("jobTitle") ?: ""
                    val pDept = doc.getString("pendingDepartment") ?: doc.getString("department") ?: ""
                    val pPhone = doc.getString("pendingPhone") ?: doc.getString("phone")
                    val pExt = doc.getString("pendingExtension") ?: doc.getString("extension")
                    val pEmail = doc.getString("pendingEmail") ?: doc.getString("email")
                    
                    val updateData = hashMapOf(
                        "name" to pName,
                        "jobTitle" to pJob,
                        "department" to pDept,
                        "phone" to pPhone,
                        "extension" to pExt,
                        "email" to pEmail,
                        "pendingName" to null,
                        "pendingJobTitle" to null,
                        "pendingDepartment" to null,
                        "pendingPhone" to null,
                        "pendingExtension" to null,
                        "pendingEmail" to null,
                        "hasPendingChanges" to false
                    )
                    db.collection("collaborators")
                        .document(collaboratorId)
                        .update(updateData as Map<String, Any>)
                        .await()
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectProfileUpdate(collaboratorId: String): Result<Unit> {
        return try {
            if (_isMockMode.value) {
                _collaborators.value = _collaborators.value.map {
                    if (it.id == collaboratorId) {
                        it.copy(
                            pendingName = null, pendingJobTitle = null, pendingDepartment = null,
                            pendingPhone = null, pendingExtension = null, pendingEmail = null,
                            hasPendingChanges = false
                        )
                    } else it
                }
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                val updateData = hashMapOf(
                    "pendingName" to null,
                    "pendingJobTitle" to null,
                    "pendingDepartment" to null,
                    "pendingPhone" to null,
                    "pendingExtension" to null,
                    "pendingEmail" to null,
                    "hasPendingChanges" to false
                )
                db.collection("collaborators")
                    .document(collaboratorId)
                    .update(updateData as Map<String, Any>)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCollaborator(collaboratorId: String): Result<Unit> {
        return try {
            if (_isMockMode.value) {
                _collaborators.value = _collaborators.value.filter { it.id != collaboratorId }
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                db.collection("collaborators")
                    .document(collaboratorId)
                    .delete()
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCurrentUserAccount(): Result<Unit> {
        return try {
            val email = _currentUserEmail.value ?: return Result.failure(IllegalStateException("Nenhum usuário logado"))
            val collaboratorId = email.replace("@coficab.com", "").replace(".", "_")
            
            if (_isMockMode.value) {
                _collaborators.value = _collaborators.value.filter { it.id != collaboratorId }
                logout()
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                db.collection("collaborators").document(collaboratorId).delete().await()
                FirebaseAuth.getInstance().currentUser?.delete()?.await()
                logout()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        if (!email.endsWith("@coficab.com")) {
            return Result.failure(IllegalArgumentException("Apenas o domínio @coficab.com é permitido."))
        }
        return try {
            if (_isMockMode.value) {
                Result.success(Unit)
            } else {
                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkForUpdates(): Result<com.example.coficall.model.AppUpdateInfo?> {
        return try {
            if (_isMockMode.value) {
                Result.success(null)
            } else {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("app_config").document("version_info").get().await()
                if (snapshot.exists()) {
                    val latestVersionCode = snapshot.getLong("latest_version_code")?.toInt() ?: 0
                    val latestVersionName = snapshot.getString("latest_version_name") ?: ""
                    val apkUrl = snapshot.getString("apk_url") ?: ""
                    val forceUpdate = snapshot.getBoolean("force_update") ?: false
                    
                    val currentVersionCode = try {
                        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            pInfo.longVersionCode.toInt()
                        } else {
                            @Suppress("DEPRECATION")
                            pInfo.versionCode
                        }
                    } catch (e: Exception) {
                        0
                    }

                    if (latestVersionCode > currentVersionCode && apkUrl.isNotBlank()) {
                        Result.success(com.example.coficall.model.AppUpdateInfo(
                            latestVersionCode = latestVersionCode,
                            latestVersionName = latestVersionName,
                            apkUrl = apkUrl,
                            forceUpdate = forceUpdate
                        ))
                    } else {
                        Result.success(null)
                    }
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun promoteVersion(versionCode: Int, versionName: String): Result<Unit> {
        return try {
            if (_isMockMode.value) {
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("app_config").document("version_info")
                val snapshot = docRef.get().await()
                if (snapshot.exists()) {
                    docRef.update(
                        "latest_version_code", versionCode,
                        "latest_version_name", versionName
                    ).await()
                } else {
                    docRef.set(
                        mapOf(
                            "latest_version_code" to versionCode,
                            "latest_version_name" to versionName,
                            "apk_url" to "https://docs.google.com/uc?export=download&id=1XixvUQfItEyzz8UBLpFC9NrF0Wbxn8g5",
                            "force_update" to false
                        )
                    ).await()
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateApkUrl(url: String): Result<Unit> {
        return try {
            if (_isMockMode.value) {
                Result.success(Unit)
            } else {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("app_config").document("version_info")
                val snapshot = docRef.get().await()
                if (snapshot.exists()) {
                    docRef.update("apk_url", url).await()
                } else {
                    docRef.set(
                        mapOf(
                            "latest_version_code" to 1,
                            "latest_version_name" to "1.0.0",
                            "apk_url" to url,
                            "force_update" to false
                        )
                    ).await()
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun saveDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    override fun loadDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }

    override suspend fun getServerVersionInfo(): Result<com.example.coficall.model.AppUpdateInfo?> {
        return try {
            if (_isMockMode.value) {
                Result.success(com.example.coficall.model.AppUpdateInfo(
                    latestVersionCode = 1,
                    latestVersionName = "1.0.0",
                    apkUrl = "https://docs.google.com/uc?export=download&id=1XixvUQfItEyzz8UBLpFC9NrF0Wbxn8g5",
                    forceUpdate = false
                ))
            } else {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("app_config").document("version_info").get().await()
                if (snapshot.exists()) {
                    val latestVersionCode = snapshot.getLong("latest_version_code")?.toInt() ?: 0
                    val latestVersionName = snapshot.getString("latest_version_name") ?: ""
                    val apkUrl = snapshot.getString("apk_url") ?: ""
                    val forceUpdate = snapshot.getBoolean("force_update") ?: false
                    Result.success(com.example.coficall.model.AppUpdateInfo(
                        latestVersionCode = latestVersionCode,
                        latestVersionName = latestVersionName,
                        apkUrl = apkUrl,
                        forceUpdate = forceUpdate
                    ))
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Helper utility to await on Tasks
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: RuntimeException("Task failed"))
        }
    }
    continuation.invokeOnCancellation {
        // Simple cancellation
    }
}
