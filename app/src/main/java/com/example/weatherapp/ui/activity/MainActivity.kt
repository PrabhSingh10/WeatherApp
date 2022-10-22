package com.example.weatherapp.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.repository.Repository
import com.example.weatherapp.ui.viewModel.WeatherViewModel
import com.example.weatherapp.ui.viewModel.WeatherViewModelProviderFactory
import com.example.weatherapp.util.Resource
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var bindingMain : ActivityMainBinding? = null
    private lateinit var locationManager : LocationManager
    private lateinit var weatherRepository : Repository
    private lateinit var viewModel: WeatherViewModel
    private val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
    private val sdf2 = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

    private val locationPermission : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permission ->
            permission.entries.forEach {
                val isGranted = it.value
                if(isGranted){
                    getLatLong()
                }else{
                    locationAlertDialog()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMain?.root)

        weatherRepository = Repository()
        val weatherViewModelProviderFactory = WeatherViewModelProviderFactory(weatherRepository)
        viewModel = ViewModelProvider(
            this,
            weatherViewModelProviderFactory
        )[WeatherViewModel::class.java]

        getPermission()

        bindingMain?.ivSearch?.setOnClickListener {
            bindingMain?.etCityName?.let {
                if(it.text.toString().isNotEmpty()){
                    getWeatherResponseCity(it.text.toString())
                    it.text.clear()
                }
            }
        }

        bindingMain?.ivUpdate?.setOnClickListener {
            getPermission()
        }

    }

    private fun locationAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply{
            setTitle("Location Permission Denied")
            setIcon(R.drawable.ic_location_alert)
            setMessage("Location permission is required")
            setPositiveButton("SETTINGS"){ dialogInterface, _ ->
                dialogInterface.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                getPermission()
            }
            setNegativeButton("CANCEL"){dialogInterface, _ ->
                dialogInterface.dismiss()
                this@MainActivity.finish()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun getLatLong() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if(hasGps && checkLocationPermission()){
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 0F){}
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(gpsLocation != null){
                getWeatherResponse(gpsLocation.latitude, gpsLocation.longitude)
            }
        }

    }

    private fun getPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
            locationAlertDialog()
        }else{
            locationPermission.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun getWeatherResponse(latitude : Double, longitude : Double) {

        viewModel.gettingWeatherDetails(latitude, longitude)

        viewModel.weather.observe(this@MainActivity, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    if(response.data != null){
                        setUpView(response.data)
                    }
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        this, response.message, Toast.LENGTH_SHORT
                    ).show()
                    hideProgressBar()
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun getWeatherResponseCity(city: String) {

        viewModel.gettingWeatherDetailsCity(city)

        viewModel.weatherCity.observe(this@MainActivity, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    if(response.data != null){
                        setUpView(response.data)
                    }
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        this, response.message, Toast.LENGTH_SHORT
                    ).show()
                    hideProgressBar()
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun setUpView(response: WeatherResponse) {
        bindingMain?.tvForecast?.text = response.weather[0].description
        val temp = (response.main.temp.toFloat() - 273.15).toInt()
        bindingMain?.tvTemp?.text = temp.toString() + "°C"
        bindingMain?.tvCity?.text = response.name
        bindingMain?.tvCountry?.text = response.sys.country
        bindingMain?.tvDate?.text = "Last Updated at " + sdf.format(Date(System.currentTimeMillis()))
        bindingMain?.tvHumidity?.text = response.main.humidity.toString()
        val minTemp = (response.main.temp_min.toFloat() - 273.15).toInt()
        bindingMain?.tvMinTemp?.text = minTemp.toString() + "°C"
        val maxTemp = (response.main.temp_max.toFloat() - 273.15).toInt()
        bindingMain?.tvMaxTemp?.text = maxTemp.toString() + "°C"
        val sunrise : Long = response.sys.sunrise.toLong()
        bindingMain?.tvSunrise?.text = sdf2.format(Date(sunrise * 1000))
        val sunset : Long = response.sys.sunset.toLong()
        bindingMain?.tvSunset?.text = sdf2.format(Date(sunset * 1000))
    }

    private fun hideProgressBar() {
        bindingMain?.progressBar?.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        bindingMain?.progressBar?.visibility = View.VISIBLE
    }
}
