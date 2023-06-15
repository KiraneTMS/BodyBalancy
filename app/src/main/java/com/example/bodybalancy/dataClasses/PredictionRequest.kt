package com.example.bodybalancy.dataClasses

import com.google.gson.annotations.SerializedName

data class PredictionRequest(
    @SerializedName("weight")
    val weight: Float,
    @SerializedName("riwayat")
    val riwayat: Int,
    @SerializedName("height")
    val height: Float,
    @SerializedName("Age")
    val Age: Int,
    @SerializedName("FAVC")
    val FAVC: Int
)
