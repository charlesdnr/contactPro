package com.example.contactpro

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class UpdateContactActivity : AppCompatActivity() {
    
    private lateinit var etNom: TextInputEditText
    private lateinit var etPrenom: TextInputEditText
    private lateinit var etSociete: TextInputEditText
    private lateinit var etAdresse: TextInputEditText
    private lateinit var etTel: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var spinnerSecteur: Spinner
    private lateinit var btnUpdate: Button
    private lateinit var btnAnnuler: Button
    
    private lateinit var database: ContactDatabase
    private var contact: Contact? = null
    private var contactId: Long = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_contact)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Modifier le contact"
        
        database = ContactDatabase.getDatabase(this)
        contactId = intent.getLongExtra("contact_id", 0)
        
        initViews()
        setupSpinner()
        setupClickListeners()
        loadContact()
    }
    
    private fun initViews() {
        etNom = findViewById(R.id.etNom)
        etPrenom = findViewById(R.id.etPrenom)
        etSociete = findViewById(R.id.etSociete)
        etAdresse = findViewById(R.id.etAdresse)
        etTel = findViewById(R.id.etTel)
        etEmail = findViewById(R.id.etEmail)
        spinnerSecteur = findViewById(R.id.spinnerSecteur)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnAnnuler = findViewById(R.id.btnAnnuler)
    }
    
    private fun setupSpinner() {
        val secteurs = arrayOf("Industrie", "Informatique", "Santé", "Commerce", "Finance", "Éducation", "Autres")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, secteurs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSecteur.adapter = adapter
    }
    
    private fun setupClickListeners() {
        btnUpdate.setOnClickListener {
            if (validateFields()) {
                updateContact()
            }
        }
        
        btnAnnuler.setOnClickListener {
            finish()
        }
    }
    
    private fun loadContact() {
        lifecycleScope.launch {
            contact = database.contactDao().getContactById(contactId)
            populateFields()
        }
    }
    
    private fun populateFields() {
        contact?.let { contact ->
            etNom.setText(contact.nom)
            etPrenom.setText(contact.prenom)
            etSociete.setText(contact.societe)
            etAdresse.setText(contact.adresse)
            etTel.setText(contact.tel)
            etEmail.setText(contact.email)
            
            // Set spinner selection
            val secteurs = arrayOf("Industrie", "Informatique", "Santé", "Commerce", "Finance", "Éducation", "Autres")
            val position = secteurs.indexOf(contact.secteur)
            if (position >= 0) {
                spinnerSecteur.setSelection(position)
            }
        }
    }
    
    private fun validateFields(): Boolean {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val societe = etSociete.text.toString().trim()
        val adresse = etAdresse.text.toString().trim()
        val tel = etTel.text.toString().trim()
        val email = etEmail.text.toString().trim()
        
        if (nom.isEmpty() || prenom.isEmpty() || societe.isEmpty() || 
            adresse.isEmpty() || tel.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun updateContact() {
        contact?.let { originalContact ->
            val updatedContact = originalContact.copy(
                nom = etNom.text.toString().trim(),
                prenom = etPrenom.text.toString().trim(),
                societe = etSociete.text.toString().trim(),
                adresse = etAdresse.text.toString().trim(),
                tel = etTel.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                secteur = spinnerSecteur.selectedItem.toString()
            )
            
            lifecycleScope.launch {
                try {
                    database.contactDao().update(updatedContact)
                    Toast.makeText(this@UpdateContactActivity, "Contact mis à jour avec succès", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@UpdateContactActivity, "Erreur lors de la mise à jour du contact: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}