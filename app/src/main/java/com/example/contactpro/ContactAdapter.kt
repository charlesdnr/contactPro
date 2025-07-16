package com.example.contactpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        val contact = contacts[position]
        
        holder.tvNomPrenom.text = "${contact.nom} ${contact.prenom}"
        holder.tvSociete.text = contact.societe
        holder.tvSecteur.text = contact.secteur
        holder.tvTel.text = contact.tel
        holder.tvEmail.text = contact.email
        
        // Set favorite icon
        if (contact.favori == 1) {
            holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_off)
        }
        
        holder.itemView.setOnClickListener {
            onContactClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}