package com.example.contactpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    
    private lateinit var tvNomPrenom: TextView
    private lateinit var tvSociete: TextView
    private lateinit var tvSecteur: TextView
    private lateinit var tvAdresse: TextView
    private lateinit var tvTel: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnToggleFavorite: Button
    private lateinit var btnCall: Button
    private lateinit var btnSms: Button
    private lateinit var btnLocation: Button
    
    private lateinit var database: ContactDatabase
    private var contact: Contact? = null
    private var contactId: Long = 0
    
    companion object {
        private const val CALL_PERMISSION_REQUEST_CODE = 1
        private const val SMS_PERMISSION_REQUEST_CODE = 2
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Détail du contact"
        
        database = ContactDatabase.getDatabase(this)
        contactId = intent.getLongExtra("contact_id", 0)
        
        initViews()
        loadContact()
        setupClickListeners()
    }
    
    private fun initViews() {
        tvNomPrenom = findViewById(R.id.tvNomPrenom)
        tvSociete = findViewById(R.id.tvSociete)
        tvSecteur = findViewById(R.id.tvSecteur)
        tvAdresse = findViewById(R.id.tvAdresse)
        tvTel = findViewById(R.id.tvTel)
        tvEmail = findViewById(R.id.tvEmail)
        btnToggleFavorite = findViewById(R.id.btnToggleFavorite)
        btnCall = findViewById(R.id.btnCall)
        btnSms = findViewById(R.id.btnSms)
        btnLocation = findViewById(R.id.btnLocation)
    }
    
    private fun loadContact() {
        lifecycleScope.launch {
            contact = database.contactDao().getContactById(contactId)
            runOnUiThread {
                displayContact()
            }
        }
    }
    
    private fun displayContact() {
        contact?.let { contact ->
            tvNomPrenom.text = "${contact.nom} ${contact.prenom}"
            tvSociete.text = "Société: ${contact.societe}"
            tvSecteur.text = "Secteur: ${contact.secteur}"
            tvAdresse.text = "Adresse: ${contact.adresse}"
            tvTel.text = "Téléphone: ${contact.tel}"
            tvEmail.text = "Email: ${contact.email}"
            
            updateFavoriteButton(contact.favori == 1)
        }
    }
    
    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            btnToggleFavorite.text = "Retirer des favoris"
        } else {
            btnToggleFavorite.text = "Ajouter aux favoris"
        }
    }
    
    private fun setupClickListeners() {
        btnToggleFavorite.setOnClickListener {
            toggleFavorite()
        }
        
        btnCall.setOnClickListener {
            makePhoneCall()
        }
        
        btnSms.setOnClickListener {
            sendSMS()
        }
        
        btnLocation.setOnClickListener {
            openLocation()
        }
    }
    
    private fun toggleFavorite() {
        contact?.let { contact ->
            val newFavoriteStatus = if (contact.favori == 1) 0 else 1
            val updatedContact = contact.copy(favori = newFavoriteStatus)
            
            lifecycleScope.launch {
                database.contactDao().update(updatedContact)
                this@DetailActivity.contact = updatedContact
                runOnUiThread {
                    updateFavoriteButton(newFavoriteStatus == 1)
                    Toast.makeText(this@DetailActivity, 
                        if (newFavoriteStatus == 1) "Ajouté aux favoris" else "Retiré des favoris", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun makePhoneCall() {
        contact?.let { contact ->
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    arrayOf(Manifest.permission.CALL_PHONE), 
                    CALL_PERMISSION_REQUEST_CODE)
            } else {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:${contact.tel}")
                startActivity(intent)
            }
        }
    }
    
    private fun sendSMS() {
        contact?.let { contact ->
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    arrayOf(Manifest.permission.SEND_SMS), 
                    SMS_PERMISSION_REQUEST_CODE)
            } else {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("smsto:${contact.tel}")
                startActivity(intent)
            }
        }
    }
    
    private fun openLocation() {
        contact?.let { contact ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("geo:0,0?q=${Uri.encode(contact.adresse)}")
            startActivity(intent)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update -> {
                contact?.let { contact ->
                    val intent = Intent(this, UpdateContactActivity::class.java)
                    intent.putExtra("contact_id", contact.id)
                    startActivity(intent)
                }
                true
            }
            R.id.action_delete -> {
                deleteContact()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun deleteContact() {
        contact?.let { contact ->
            lifecycleScope.launch {
                database.contactDao().delete(contact)
                runOnUiThread {
                    Toast.makeText(this@DetailActivity, "Contact supprimé", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALL_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall()
                } else {
                    Toast.makeText(this, "Permission d'appel refusée", Toast.LENGTH_SHORT).show()
                }
            }
            SMS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMS()
                } else {
                    Toast.makeText(this, "Permission SMS refusée", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    override fun onResume() {
        super.onResume()
        loadContact()
    }
}