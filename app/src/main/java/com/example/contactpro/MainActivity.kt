package com.example.contactpro

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var database: ContactDatabase
    private var showingFavorites = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        database = ContactDatabase.getDatabase(this)
        
        setupRecyclerView()
        loadContacts()
    }
    
    override fun onResume() {
        super.onResume()
        loadContacts()
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
    
    private fun loadContacts() {
        lifecycleScope.launch {
            val contacts = if (showingFavorites) {
                database.contactDao().getFavoriteContacts()
            } else {
                database.contactDao().getAllContacts()
            }
            
            runOnUiThread {
                contactAdapter.updateContacts(contacts)
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_contact -> {
                val intent = Intent(this, AddContactActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_show_favorites -> {
                showingFavorites = true
                loadContacts()
                true
            }
            R.id.action_show_all -> {
                showingFavorites = false
                loadContacts()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}