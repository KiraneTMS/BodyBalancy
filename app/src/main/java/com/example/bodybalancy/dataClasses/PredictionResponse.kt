package com.example.bodybalancy.dataClasses

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("prediction")
    val prediction: String
)

