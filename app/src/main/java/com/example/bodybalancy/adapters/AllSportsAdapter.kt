package com.example.bodybalancy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bodybalancy.R

class AllSportsAdapter(private var sports: List<Map<String, String>>) :
    RecyclerView.Adapter<AllSportsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sport, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sport = sports[position]
        holder.bind(sport)
    }

    override fun getItemCount(): Int {
        return sports.size
    }

    fun updateData(newSports: List<Map<String, String>>) {
        sports = newSports
        notifyDataSetChanged()
    }
    fun clearData() {
        sports = emptyList()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(sport: Map<String, String>) {
            val sportName = sport["name"]
            // Bind the data to the views in the item layout
            itemView.findViewById<TextView>(R.id.txtSportName).text = sportName
        }
    }

}