package com.desafiolatam.weatherlatam.data.remote

import com.desafiolatam.weatherlatam.data.OPEN_WEATHER_ENDPOINT
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun getInstance(): Retrofit {
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder().baseUrl(OPEN_WEATHER_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }
}