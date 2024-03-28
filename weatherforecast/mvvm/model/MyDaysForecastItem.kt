package com.nikita_zayanchkovskij.weatherforecast.mvvm.model


data class MyDaysForecastItem (

    val city: String,
    val dtTxtButOnlyDate: String,
    val weatherConditionDescription: String,
    val maxTemp: String,
    val minTemp: String,
    val icon: String

)
