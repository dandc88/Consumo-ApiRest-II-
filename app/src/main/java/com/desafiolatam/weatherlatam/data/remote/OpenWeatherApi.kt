package com.desafiolatam.weatherlatam.data.remote

import com.desafiolatam.weatherlatam.data.local.WeatherWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface OpenWeatherService {

    @GET("weather")
    suspend fun getWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String,
        @Query("units") unit: String = "metric"
    ): Response<WeatherWrapper?>
}