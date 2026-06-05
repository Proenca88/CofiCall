package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CollaboratorDao {
    @Query("SELECT * FROM collaborators ORDER BY name ASC")
    fun getAllCollaborators(): Flow<List<Collaborator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborators(collaborators: List<Collaborator>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborator(collaborator: Collaborator)

    @Update
    suspend fun updateCollaborator(collaborator: Collaborator)

    @Delete
    suspend fun deleteCollaborator(collaborator: Collaborator)
}
