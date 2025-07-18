package com.example.contactpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onContactClick: (Contact) -> Unit,
    private val onFavoriteClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInitials: TextView = itemView.findViewById(R.id.tvInitials)
        val tvNomPrenom: TextView = itemView.findViewById(R.id.tvNomPrenom)
        val tvSociete: TextView = itemView.findViewById(R.id.tvSociete)
        val tvSecteur: TextView = itemView.findViewById(R.id.tvSecteur)
        val tvTel: TextView = itemView.findViewById(R.id.tvTel)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        try {
            val contact = contacts[position]
            
            // Set initials safely
            val nom = contact.nom.takeIf { it != "Non spécifié" && it.isNotEmpty() } ?: ""
            val prenom = contact.prenom.takeIf { it != "Non spécifié" && it.isNotEmpty() } ?: ""
            val initials = "${nom.firstOrNull()?.uppercase() ?: ""}${prenom.firstOrNull()?.uppercase() ?: ""}"
            holder.tvInitials.text = if (initials.isNotEmpty()) initials else "?"
            
            holder.tvNomPrenom.text = "${contact.nom} ${contact.prenom}"
            holder.tvSociete.text = contact.societe.takeIf { it != "Non spécifié" && it.isNotEmpty() } ?: "Société non renseignée"
            holder.tvSecteur.text = contact.secteur.takeIf { it.isNotEmpty() } ?: "Secteur non spécifié"
            holder.tvTel.text = contact.tel.takeIf { it != "Non spécifié" && it.isNotEmpty() } ?: "N/A"
            holder.tvEmail.text = contact.email.takeIf { it != "Non spécifié" && it.isNotEmpty() } ?: "N/A"
            
            // Set favorite icon
            if (contact.favori == 1) {
                holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_off)
            }
            
            holder.itemView.setOnClickListener {
                onContactClick(contact)
            }
            
            holder.ivFavorite.setOnClickListener {
                onFavoriteClick(contact)
            }
        } catch (e: Exception) {
            // Fallback values in case of any error
            holder.tvInitials.text = "?"
            holder.tvNomPrenom.text = "Contact invalide"
            holder.tvSociete.text = "Erreur"
            holder.tvSecteur.text = "Erreur"
            holder.tvTel.text = "N/A"
            holder.tvEmail.text = "N/A"
            holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
    
    fun getContactAt(position: Int): Contact {
        return contacts[position]
    }
}