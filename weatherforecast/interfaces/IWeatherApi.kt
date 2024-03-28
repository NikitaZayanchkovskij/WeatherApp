package com.nikita_zayanchkovskij.weatherforecast.interfaces


import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.CurrentDayWeatherData
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.FiveDaysAndThreeHoursWeatherData
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.GeocodingData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface IWeatherApi {

    @GET("weather")
    suspend fun getCurrentWeatherData(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("appid") myWeatherApiId: String,
        @Query("units") unitsOfMeasurement: String,
        @Query("lang") language: String) : Response<CurrentDayWeatherData>

    @GET("forecast")
    suspend fun getForecastWeatherDataFor5Days(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("appid") myWeatherApiId: String,
        @Query("units") unitsOfMeasurement: String,
        @Query("lang") language: String) : Response<FiveDaysAndThreeHoursWeatherData>


    @GET("direct")
    suspend fun getWeatherDataInRequestedCity(
        @Query("q") cityName: String,
        @Query("limit") limit: String,
        @Query("appid") myWeatherApiId: String) : Response<List<GeocodingData>>

}