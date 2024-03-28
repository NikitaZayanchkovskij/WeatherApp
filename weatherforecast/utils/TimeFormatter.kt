package com.nikita_zayanchkovskij.weatherforecast.utils


import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


object TimeFormatter {


    fun convertTimeInMillisToDateAndTime(dtParameterFromWeatherApi: Long): String {

        @SuppressLint("ConstantLocale")
        val timeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = dtParameterFromWeatherApi * 1000

        return timeFormatter.format(calendar.time)
    }


    fun convertTimeInMillisToTimeOnly(sunriseOrSunsetParameterFromWeatherApi: Long): String {

        @SuppressLint("ConstantLocale")
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = sunriseOrSunsetParameterFromWeatherApi * 1000

        return timeFormatter.format(calendar.time)
    }


}