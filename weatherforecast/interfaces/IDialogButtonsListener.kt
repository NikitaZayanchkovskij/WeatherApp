package com.nikita_zayanchkovskij.weatherforecast.interfaces


interface IDialogButtonsListener {

    fun positiveButtonPressed(cityName: String?)
    fun negativeButtonPressed()

}