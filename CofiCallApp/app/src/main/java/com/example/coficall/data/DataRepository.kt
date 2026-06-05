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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface DataRepository {
    val isLoggedIn: StateFlow<Boolean>
    val currentUserEmail: StateFlow<String?>
    val isOfflineMode: StateFlow<Boolean>
    val isMockMode: StateFlow<Boolean>
    
    val collaborators: StateFlow<List<Collaborator>>
    val businessUnits: StateFlow<List<BusinessUnit>>
    
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun logout()
    suspend fun toggleFavorite(collaborator: Collaborator)
    suspend fun refreshData()
}

class DefaultDataRepository(private val context: Context) : DataRepository {
    private val prefs = context.getSharedPreferences("coficall_prefs", Context.MODE_PRIVATE)
    
    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

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

    private var collaboratorsRegistration: ListenerRegistration? = null
    private var businessUnitsRegistration: ListenerRegistration? = null

    private val favoriteIds: MutableSet<String>
        get() = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()

    init {
        checkFirebaseAvailability()
    }

    private fun checkFirebaseAvailability() {
        val isFirebaseAvailable = try {
            FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            false
        }

        if (isFirebaseAvailable) {
            _isMockMode.value = false
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                _isLoggedIn.value = true
                _currentUserEmail.value = currentUser.email
                startFirebaseSync()
            } else {
                _isLoggedIn.value = false
                _currentUserEmail.value = null
            }
            
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    _isLoggedIn.value = true
                    _currentUserEmail.value = user.email
                    startFirebaseSync()
                } else {
                    _isLoggedIn.value = false
                    _currentUserEmail.value = null
                    stopFirebaseSync()
                    _collaborators.value = emptyList()
                    _businessUnits.value = emptyList()
                }
            }
        } else {
            _isMockMode.value = true
            val mockLoggedIn = prefs.getBoolean("mock_logged_in", false)
            _isLoggedIn.value = mockLoggedIn
            _currentUserEmail.value = if (mockLoggedIn) prefs.getString("mock_email", "colaborador@coficab.com") else null
            
            if (mockLoggedIn) {
                loadMockData()
            }
        }
    }

    private fun loadMockData() {
        val favs = favoriteIds
        val mappedCollaborators = sampleCollaborators.map {
            it.copy(isFavorite = favs.contains(it.id))
        }
        _collaborators.value = mappedCollaborators
        _businessUnits.value = sampleBusinessUnits
        _isOfflineMode.value = false
    }

    private fun startFirebaseSync() {
        stopFirebaseSync()

        val db = FirebaseFirestore.getInstance()
        
        collaboratorsRegistration = db.collection("collaborators")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DefaultDataRepository", "Error syncing collaborators", error)
                    _isOfflineMode.value = true
                    return@addSnapshotListener
                }

                if (snapshot != null) {
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
                                isOnline = isOnline
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _collaborators.value = list
                    _isOfflineMode.value = snapshot.metadata.isFromCache
                }
            }

        businessUnitsRegistration = db.collection("businessUnits")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DefaultDataRepository", "Error syncing business units", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
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
                                name = name,
                                shortName = shortName,
                                country = country,
                                collaboratorCount = count,
                                type = type
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _businessUnits.value = list
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
