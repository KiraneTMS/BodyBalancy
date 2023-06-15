package com.example.bodybalancy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bodybalancy.R

class SportCategoriesAdapter(
    private var sportCategories: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SportCategoriesAdapter.ViewHolder>() {

    private var selectedCategory: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sport_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sportCategory = sportCategories[position]
        holder.bind(sportCategory)
    }

    override fun getItemCount(): Int {
        return sportCategories.size
    }

    fun updateData(newData: List<String>) {
        sportCategories = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtSportCategory: TextView = itemView.findViewById(R.id.txtSportCategory)

        fun bind(sportCategory: String) {
            txtSportCategory.text = sportCategory

            if (sportCategory == selectedCategory) {
                // Set selected category text style or any other customization
            } else {
                // Set default category text style or any other customization
            }

            itemView.setOnClickListener {
                selectedCategory = sportCategory
                onItemClick(sportCategory)
                println(selectedCategory)
                notifyDataSetChanged()
            }
        }
    }
}