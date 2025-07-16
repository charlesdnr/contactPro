package com.example.contactpro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val prenom: String,
    val societe: String,
    val adresse: String,
    val tel: String,
    val email: String,
    val secteur: String,
    val favori: Int = 0
)