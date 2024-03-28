package com.nikita_zayanchkovskij.weatherforecast.mvvm.model


/** Этот класс принимает ответ с https://openweathermap.org/current с данными прогноза погоды на
 * сегодняшний день.
 */
data class CurrentDayWeatherData (
    val coord: CoordCurrentData,
    val weather: List<WeatherCurrentData>,
    val main: MainCurrentData,
    val wind: WindCurrentData,
    val dt: Long,
    val sys: SysCurrentData,
    val timezone: Int,
    val id: Int,
    val name: String /* Город */
)


data class CoordCurrentData (
    val lon: Float,
    val lat: Float
)


data class WeatherCurrentData (
    val description: String, /* Например sunny, moderate rain и т.д. */
    val icon: String
)


data class MainCurrentData (
    val temp: Float,
    val feels_like: Float,
    val temp_min: Float,
    val temp_max: Float,
    val pressure: Int,
    val humidity: Int
)


data class WindCurrentData (
    val speed: Float
)


data class SysCurrentData (
    val country: String,
    val sunrise: Long,
    val sunset: Long
)