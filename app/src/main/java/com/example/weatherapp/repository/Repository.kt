package com.example.weatherapp.repository

import com.example.weatherapp.api.RetrofitInstance

class Repository {
    suspend fun getWeatherDetails(latitude : Double, longitude : Double) =
        RetrofitInstance.weatherApi.getWeatherDetails(latitude, longitude)

    suspend fun getWeatherDetailsCity(city : String) =
        RetrofitInstance.weatherApi.getWeatherDetailsCity(city)
}