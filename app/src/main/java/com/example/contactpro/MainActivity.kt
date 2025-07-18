package com.example.contactpro

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
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
        
        contactAdapter = ContactAdapter(emptyList(), { contact ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contact_id", contact.id)
            startActivity(intent)
        }, { contact ->
            toggleFavorite(contact)
        })
        
        recyclerView.adapter = contactAdapter
        
        setupSwipeToDelete()
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
    
    private fun toggleFavorite(contact: Contact) {
        lifecycleScope.launch {
            try {
                val updatedContact = contact.copy(favori = if (contact.favori == 1) 0 else 1)
                database.contactDao().update(updatedContact)
                loadContacts()
            } catch (e: Exception) {
                // En cas d'erreur, ne rien faire
            }
        }
    }
    
    private fun setupSwipeToDelete() {
        val deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)
        val deleteBackground = ColorDrawable(Color.RED)
        
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val contact = contactAdapter.getContactAt(position)
                
                showDeleteConfirmation(contact, position)
            }
            
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                val iconBottom = iconTop + deleteIcon.intrinsicHeight
                
                val swipeThreshold = itemView.width * 0.3f
                
                if (dX < 0) { // Swipe vers la gauche
                    val swipeProgress = Math.abs(dX) / itemView.width
                    
                    if (Math.abs(dX) > swipeThreshold) {
                        // Gros swipe - suppression directe
                        deleteBackground.setBounds(
                            itemView.right + dX.toInt(),
                            itemView.top,
                            itemView.right,
                            itemView.bottom
                        )
                        deleteBackground.draw(c)
                        
                        val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        deleteIcon.draw(c)
                        
                        // Si le swipe est très important, déclencher la suppression
                        if (Math.abs(dX) > itemView.width * 0.7f && isCurrentlyActive) {
                            deleteContact(contactAdapter.getContactAt(viewHolder.bindingAdapterPosition))
                            return
                        }
                    } else {
                        // Petit swipe - afficher l'icône seulement
                        val alpha = (swipeProgress * 255).toInt()
                        deleteBackground.alpha = alpha
                        deleteBackground.setBounds(
                            itemView.right + dX.toInt(),
                            itemView.top,
                            itemView.right,
                            itemView.bottom
                        )
                        deleteBackground.draw(c)
                        
                        deleteIcon.alpha = alpha
                        val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        deleteIcon.draw(c)
                    }
                }
                
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
            
            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.7f // Seuil pour déclencher la suppression automatique
            }
        })
        
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    
    private fun showDeleteConfirmation(contact: Contact, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer le contact")
            .setMessage("Voulez-vous vraiment supprimer ${contact.nom} ${contact.prenom} ?")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteContact(contact)
            }
            .setNegativeButton("Annuler") { _, _ ->
                // Restaurer l'élément à sa position
                contactAdapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                // Restaurer l'élément à sa position si l'utilisateur annule
                contactAdapter.notifyItemChanged(position)
            }
            .show()
    }
    
    private fun deleteContact(contact: Contact) {
        lifecycleScope.launch {
            try {
                database.contactDao().delete(contact)
                loadContacts()
            } catch (e: Exception) {
                // En cas d'erreur, recharger la liste
                loadContacts()
            }
        }
    }
}