package com.example.contactpro

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddContactActivity : AppCompatActivity() {
    
    private lateinit var etNom: TextInputEditText
    private lateinit var etPrenom: TextInputEditText
    private lateinit var etSociete: TextInputEditText
    private lateinit var etAdresse: TextInputEditText
    private lateinit var etTel: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var spinnerSecteur: Spinner
    private lateinit var btnAjouter: Button
    private lateinit var btnAnnuler: Button
    
    private lateinit var database: ContactDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)
        
        database = ContactDatabase.getDatabase(this)
        
        initViews()
        setupSpinner()
        setupClickListeners()
    }
    
    private fun initViews() {
        etNom = findViewById(R.id.etNom)
        etPrenom = findViewById(R.id.etPrenom)
        etSociete = findViewById(R.id.etSociete)
        etAdresse = findViewById(R.id.etAdresse)
        etTel = findViewById(R.id.etTel)
        etEmail = findViewById(R.id.etEmail)
        spinnerSecteur = findViewById(R.id.spinnerSecteur)
        btnAjouter = findViewById(R.id.btnAjouter)
        btnAnnuler = findViewById(R.id.btnAnnuler)
    }
    
    private fun setupSpinner() {
        val secteurs = arrayOf("Industrie", "Informatique", "Santé", "Commerce", "Finance", "Éducation", "Autres")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, secteurs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSecteur.adapter = adapter
    }
    
    private fun setupClickListeners() {
        btnAjouter.setOnClickListener {
            if (validateFields()) {
                saveContact()
            }
        }
        
        btnAnnuler.setOnClickListener {
            finish()
        }
    }
    
    private fun validateFields(): Boolean {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val tel = etTel.text.toString().trim()
        val email = etEmail.text.toString().trim()
        
        if (nom.isEmpty() && prenom.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir au moins le nom ou le prénom", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (tel.isEmpty() && email.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir au moins le téléphone ou l'email", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun saveContact() {
        val contact = Contact(
            nom = etNom.text.toString().trim().ifEmpty { "Non spécifié" },
            prenom = etPrenom.text.toString().trim().ifEmpty { "Non spécifié" },
            societe = etSociete.text.toString().trim().ifEmpty { "Non spécifié" },
            adresse = etAdresse.text.toString().trim().ifEmpty { "Non spécifié" },
            tel = etTel.text.toString().trim().ifEmpty { "Non spécifié" },
            email = etEmail.text.toString().trim().ifEmpty { "Non spécifié" },
            secteur = spinnerSecteur.selectedItem.toString(),
            favori = 0
        )
        
        lifecycleScope.launch {
            try {
                database.contactDao().insert(contact)
                Toast.makeText(this@AddContactActivity, "Contact ajouté avec succès", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddContactActivity, "Erreur lors de l'ajout du contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
}