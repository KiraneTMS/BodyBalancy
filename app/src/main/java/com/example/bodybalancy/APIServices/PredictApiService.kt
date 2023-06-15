package com.example.bodybalancy.APIServices

import com.example.bodybalancy.Interfaces.PredictApiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PredictApiService {
    private const val BASE_URL = "https://obesty-model-fm5o2c5urq-et.a.run.app/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: PredictApiInterface by lazy {
        retrofit.create(PredictApiInterface::class.java)
    }
}
