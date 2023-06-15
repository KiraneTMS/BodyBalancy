package com.example.bodybalancy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bodybalancy.R
import com.example.bodybalancy.dataClasses.RecommendedFood

class RecommendedFoodAdapter(private val foods: List<RecommendedFood>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HEADER_VIEW_TYPE = 0
    private val FOOD_VIEW_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recommended_food_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recommended_food, parent, false)
                FoodViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            // Bind the header view
            holder.bindHeader()
        } else if (holder is FoodViewHolder) {
            // Bind the food item view
            val food = foods[position - 1] // Adjust the position to skip the header
            holder.bindFood(food)
        }
    }

    override fun getItemCount(): Int {
        // Add 1 to the item count to include the header
        return foods.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        // Use different view types for the header and food items
        return if (position == 0) HEADER_VIEW_TYPE else FOOD_VIEW_TYPE
    }

    // ViewHolder for the header view
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindHeader() {
            // Bind the header view here
            // For example:
            val headerTextView = itemView.findViewById<TextView>(R.id.textViewFoodName)
            headerTextView.text = "Name"
            val CaloriesTextView = itemView.findViewById<TextView>(R.id.textViewFoodName)
            CaloriesTextView.text = "Calories"
        }
    }

    // ViewHolder for the food item view
    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindFood(food: RecommendedFood) {
            // Bind the food item view here
            // For example:
            val foodNameTextView = itemView.findViewById<TextView>(R.id.textViewFoodName)
            val foodCaloriesTextView = itemView.findViewById<TextView>(R.id.textViewFoodCalories)

            foodNameTextView.text = food.foodName
            foodCaloriesTextView.text = food.foodCalories.toString()
        }
    }
}