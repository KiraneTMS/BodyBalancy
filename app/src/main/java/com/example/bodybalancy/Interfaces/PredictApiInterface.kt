package com.example.bodybalancy.Interfaces

import com.example.bodybalancy.dataClasses.PredictionResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PredictApiInterface {
    @FormUrlEncoded
    @POST("predict")
    fun predict(
        @Field("weight") weight: Float,
        @Field("riwayat") riwayat: Int,
        @Field("height") height: Float,
        @Field("Age") age: Int,
        @Field("FAVC") favc: Int
    ): Call<PredictionResponse>
}
