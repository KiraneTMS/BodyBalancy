package com.example.bodybalancy.dataClasses

import com.google.gson.annotations.SerializedName

data class FoodRecommendationResponse(
    @SerializedName("Rekomendasi")
    val recommendation: RecommendationData
)

data class RecommendationData(
    @SerializedName("BMR")
    val bmr: Double,

    @SerializedName("Daily Calorie Intake")
    val dailyCalorieIntake: Double,

    @SerializedName("Recommended Foods")
    val recommendedFoods: List<List<Any>>
)



