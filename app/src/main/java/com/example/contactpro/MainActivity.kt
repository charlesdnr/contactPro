package com.example.contactpro

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var database: ContactDatabase
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private var showingFavorites = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        database = ContactDatabase.getDatabase(this)
        
        setupViews()
        setupRecyclerView()
        setupBottomNavigation()
        loadContacts()
    }
    
    override fun onResume() {
        super.onResume()
        loadContacts()
    }
    
    private fun setupViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        contactAdapter = ContactAdapter(emptyList()) { contact ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contact_id", contact.id)
            startActivity(intent)
        }
        
        recyclerView.adapter = contactAdapter
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_contacts -> {
                    showingFavorites = false
                    tvTitle.text = "Mes Contacts"
                    tvSubtitle.text = "Gérez vos contacts facilement"
                    loadContacts()
                    true
                }
                R.id.nav_favorites -> {
                    showingFavorites = true
                    tvTitle.text = "Mes Favoris"
                    tvSubtitle.text = "Vos contacts préférés"
                    loadContacts()
                    true
                }
                R.id.nav_add -> {
                    val intent = Intent(this, AddContactActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        
        // Set default selection
        bottomNavigation.selectedItemId = R.id.nav_contacts
    }
    
    private fun loadContacts() {
        lifecycleScope.launch {
            try {
                val contacts = if (showingFavorites) {
                    database.contactDao().getFavoriteContacts()
                } else {
                    database.contactDao().getAllContacts()
                }
                
                contactAdapter.updateContacts(contacts ?: emptyList())
            } catch (e: Exception) {
                // En cas d'erreur, charger une liste vide
                contactAdapter.updateContacts(emptyList())
            }
        }
    }
}