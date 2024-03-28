package com.nikita_zayanchkovskij.weatherforecast.mvvm.model


/** Этот класс принимает ответ с https://openweathermap.org/forecast5 с данными прогноза погоды на
 * 5 дней и с данными на каждые 3 часа в дне.
 */
data class FiveDaysAndThreeHoursWeatherData (

    /* В этом списке list на первой позиции будут данные 1ого дня на 12 часов например,
     * на 2ой позиции данные тоже 1ого дня, но на 15 часов, на 3ей - 1ого дня на 18 часов,
     * на 4ой - 1ого дня на 21 час, на 5ой - 1ого дня на 24 часа,
     * на 6ой - данные УЖЕ 2ого дня на 3 часа ночи, и т.д. и т.п.
     */
    val list: List<ListData>,
    val city: CityData /* Город, широта, долгота. */
)


data class ListData (
    val dt: Long, /* 1661871600 Time of data forecasted, unix, UTC */
    val main: MainData,
    val weather: List<WeatherData>,
    val wind: WindData,
    val dt_txt: String /* 2024-03-03 12:00:00 Time of data forecasted, ISO, UTC */
)


data class MainData (
    val temp: Float,
    val feels_like: Float,
    val temp_min: Float,
    val temp_max: Float,
    val pressure: Int,
    val humidity: Int
)


data class WeatherData (
    val description: String, /* Например sunny, light rain и т.д. */
    val icon: String /* Иконка погоды: солнце, солнце с тучей и т.д. */
)


data class WindData (
    val speed: Float
)


data class CityData (
    val name: String,
    val coord: CoordData,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long, /* Время восхода в таком формате: 1661834187 */
    val sunset: Long /* Время заката в таком формате: 1661834187 */
)


data class CoordData (
    val lat: Float,
    val lon: Float
)