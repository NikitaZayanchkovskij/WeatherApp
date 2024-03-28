package com.nikita_zayanchkovskij.weatherforecast.mvvm.model


data class GeocodingData(

    val name: String,
    val lat: Float,
    val lon: Float,
    val country: String,
    val state: String

)
