package com.example.bodybalancy.Interfaces

import com.example.bodybalancy.dataClasses.FoodRecommendationResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FoodRecommendationApiService {
    @FormUrlEncoded
    @POST("recommend")
    fun getFoodRecommendation(
        @Field("height") height: Int,
        @Field("weight") weight: Int,
        @Field("age") age: Int,
        @Field("gender") gender: String,
        @Field("activity_level") activityLevel: String
    ): Call<FoodRecommendationResponse>
}
