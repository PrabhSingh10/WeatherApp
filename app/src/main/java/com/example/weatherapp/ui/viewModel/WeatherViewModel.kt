package com.example.weatherapp.ui.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.repository.Repository
import com.example.weatherapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel(private val repository: Repository)
    : ViewModel(){

    val weather = MutableLiveData<Resource<WeatherResponse>>()
    val weatherCity = MutableLiveData<Resource<WeatherResponse>>()

    fun gettingWeatherDetails(latitude : Double, longitude : Double) = viewModelScope.launch {

        weather.postValue(Resource.Loading())

        val result = repository.getWeatherDetails(latitude, longitude)

        weather.postValue(handlingWeatherDetails(result))

    }

    fun gettingWeatherDetailsCity(city : String) = viewModelScope.launch {

        weatherCity.postValue(Resource.Loading())

        val result = repository.getWeatherDetailsCity(city)

        weatherCity.postValue(handlingWeatherDetails(result))

    }

    private fun handlingWeatherDetails(result: Response<WeatherResponse>):Resource<WeatherResponse>{

        if(result.isSuccessful) {
            result.body()?.let {
                return Resource.Success(it)
            }
        }

        return Resource.Failure(result.message())

    }

}