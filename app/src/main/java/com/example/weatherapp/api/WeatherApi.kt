package com.example.weatherapp.api

import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.util.Constants.Companion.API
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("/data/2.5/weather")
    suspend fun getWeatherDetails(
        @Query("lat")
        latitude : Double,
        @Query("lon")
        longitude : Double,
        @Query("appid")
        apiKey : String = API
    ) : Response<WeatherResponse>

    @GET("/data/2.5/weather")
    suspend fun getWeatherDetailsCity(
        @Query("q")
        city : String,
        @Query("appid")
        apiKey : String = API
    ) : Response<WeatherResponse>

}