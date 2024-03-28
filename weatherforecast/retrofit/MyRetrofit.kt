package com.nikita_zayanchkovskij.weatherforecast.retrofit


import com.nikita_zayanchkovskij.weatherforecast.interfaces.IWeatherApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object MyRetrofit {


    fun initRetrofit(baseUrl: String): IWeatherApi {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(IWeatherApi::class.java)

        return weatherApi
    }


}