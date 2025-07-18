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
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
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
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvInitials: TextView
    private lateinit var webViewMap: WebView
    private lateinit var tvMapAddress: TextView
    
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
        setupBottomNavigation()
        setupWebView()
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
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        tvInitials = findViewById(R.id.tvInitials)
        webViewMap = findViewById(R.id.webViewMap)
        tvMapAddress = findViewById(R.id.tvMapAddress)
    }
    
    private fun loadContact() {
        lifecycleScope.launch {
            contact = database.contactDao().getContactById(contactId)
            displayContact()
        }
    }
    
    private fun displayContact() {
        contact?.let { contact ->
            tvNomPrenom.text = "${contact.nom} ${contact.prenom}"
            tvSociete.text = if (contact.societe != "Non spécifié") contact.societe else "Société non renseignée"
            tvSecteur.text = if (contact.secteur.isNotEmpty()) contact.secteur else "Secteur non spécifié"
            tvAdresse.text = if (contact.adresse != "Non spécifié") contact.adresse else "Adresse non renseignée"
            tvTel.text = if (contact.tel != "Non spécifié") contact.tel else "Téléphone non renseigné"
            tvEmail.text = if (contact.email != "Non spécifié") contact.email else "Email non renseigné"
            
            // Set initials
            val nom = contact.nom.takeIf { it != "Non spécifié" && it.isNotEmpty() } ?: ""
            val prenom = contact.prenom.takeIf { it != "Non spécifié" && it.isNotEmpty() } ?: ""
            val initials = "${nom.firstOrNull()?.uppercase() ?: ""}${prenom.firstOrNull()?.uppercase() ?: ""}"
            tvInitials.text = if (initials.isNotEmpty()) initials else "?"
            
            updateFavoriteButton(contact.favori == 1)
            loadMap(contact.adresse)
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
        
        btnEdit.setOnClickListener {
            editContact()
        }
        
        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }
    
    private fun toggleFavorite() {
        contact?.let { contact ->
            val newFavoriteStatus = if (contact.favori == 1) 0 else 1
            val updatedContact = contact.copy(favori = newFavoriteStatus)
            
            lifecycleScope.launch {
                database.contactDao().update(updatedContact)
                this@DetailActivity.contact = updatedContact
                updateFavoriteButton(newFavoriteStatus == 1)
                Toast.makeText(this@DetailActivity, 
                    if (newFavoriteStatus == 1) "Ajouté aux favoris" else "Retiré des favoris", 
                    Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@DetailActivity, "Contact supprimé", Toast.LENGTH_SHORT).show()
                finish()
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
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_contacts -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_favorites -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("show_favorites", true)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_add -> {
                    val intent = Intent(this, AddContactActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupWebView() {
        webViewMap.webViewClient = WebViewClient()
        webViewMap.settings.javaScriptEnabled = true
        webViewMap.settings.domStorageEnabled = true
    }
    
    private fun loadMap(address: String) {
        tvMapAddress.text = address
        
        if (address.isEmpty() || address == "Non spécifié" || address == "Adresse non renseignée") {
            // Afficher une map par défaut (Paris)
            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { margin: 0; padding: 0; }
                        #map { width: 100%; height: 200px; }
                        .no-address { 
                            display: flex; 
                            align-items: center; 
                            justify-content: center; 
                            height: 200px; 
                            background: #f0f0f0;
                            font-family: Arial, sans-serif;
                            color: #666;
                        }
                    </style>
                </head>
                <body>
                    <div class="no-address">
                        <p>Adresse non renseignée</p>
                    </div>
                </body>
                </html>
            """.trimIndent()
            
            webViewMap.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } else {
            // Utiliser OpenStreetMap avec Leaflet (gratuit)
            val encodedAddress = Uri.encode(address)
            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
                    <style>
                        body { margin: 0; padding: 0; }
                        #map { width: 100%; height: 200px; }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
                    <script>
                        // Initialiser la carte
                        var map = L.map('map').setView([48.8566, 2.3522], 13);
                        
                        // Ajouter les tuiles OpenStreetMap
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '© OpenStreetMap contributors'
                        }).addTo(map);
                        
                        // Géocoder l'adresse
                        fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent('$address'))
                            .then(response => response.json())
                            .then(data => {
                                if (data.length > 0) {
                                    var lat = parseFloat(data[0].lat);
                                    var lon = parseFloat(data[0].lon);
                                    
                                    // Centrer la carte sur l'adresse
                                    map.setView([lat, lon], 15);
                                    
                                    // Ajouter un marqueur
                                    L.marker([lat, lon]).addTo(map)
                                        .bindPopup('$address')
                                        .openPopup();
                                } else {
                                    // Si l'adresse n'est pas trouvée, afficher un message
                                    document.getElementById('map').innerHTML = 
                                        '<div style="display: flex; align-items: center; justify-content: center; height: 200px; background: #f0f0f0; font-family: Arial, sans-serif; color: #666;">' +
                                        '<p>Adresse non trouvée</p>' +
                                        '</div>';
                                }
                            })
                            .catch(error => {
                                console.error('Erreur:', error);
                                document.getElementById('map').innerHTML = 
                                    '<div style="display: flex; align-items: center; justify-content: center; height: 200px; background: #f0f0f0; font-family: Arial, sans-serif; color: #666;">' +
                                    '<p>Erreur de chargement</p>' +
                                    '</div>';
                            });
                    </script>
                </body>
                </html>
            """.trimIndent()
            
            webViewMap.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
    }
    
    private fun editContact() {
        contact?.let { contact ->
            val intent = Intent(this, UpdateContactActivity::class.java)
            intent.putExtra("contact_id", contact.id)
            startActivity(intent)
        }
    }
    
    private fun showDeleteConfirmation() {
        contact?.let { contact ->
            AlertDialog.Builder(this)
                .setTitle("Supprimer le contact")
                .setMessage("Voulez-vous vraiment supprimer ${contact.nom} ${contact.prenom} ?\n\nCette action est irréversible.")
                .setPositiveButton("Supprimer") { _, _ ->
                    deleteContactFromView()
                }
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }
    
    private fun deleteContactFromView() {
        contact?.let { contact ->
            lifecycleScope.launch {
                try {
                    database.contactDao().delete(contact)
                    Toast.makeText(this@DetailActivity, "Contact supprimé avec succès", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@DetailActivity, "Erreur lors de la suppression: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}