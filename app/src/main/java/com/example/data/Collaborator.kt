package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collaborators")
data class Collaborator(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val department: String,
    val company: String, // "COF PT", "COF GR", "CoE PT", "CoE GR", etc.
    val email: String,
    val phone: String,
    val photoUrl: String,
    val isFavorite: Boolean = false,
    val status: String = "online", // "online" or "offline"
    val isFactory: Boolean = false, // true = Factory, false = Office
    val isOffice: Boolean = false // true = Office
)
