package com.example.contactpro

import androidx.room.*

@Dao
interface ContactDAO {
    
    @Insert
    suspend fun insert(contact: Contact): Long
    
    @Update
    suspend fun update(contact: Contact)
    
    @Delete
    suspend fun delete(contact: Contact)
    
    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<Contact>
    
    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): Contact?
    
    @Query("SELECT * FROM contacts WHERE favori = 1")
    suspend fun getFavoriteContacts(): List<Contact>
    
    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Long)
}