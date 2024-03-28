package com.nikita_zayanchkovskij.weatherforecast.mvvm.viewmodel


import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nikita_zayanchkovskij.weatherforecast.constants.MyConstants
import com.nikita_zayanchkovskij.weatherforecast.interfaces.IWeatherApi
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.CurrentDayWeatherData
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.FiveDaysAndThreeHoursWeatherData
import com.nikita_zayanchkovskij.weatherforecast.retrofit.MyRetrofit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Locale


class MyViewModel: ViewModel() {
    val currentDayWeatherData = MutableLiveData<CurrentDayWeatherData>()
    val fiveDaysWeatherData = MutableLiveData<FiveDaysAndThreeHoursWeatherData>()

    /* На MainActivity когда получил местоположение пользователя - сохраняю сюда координаты, чтобы
     * был к ним доступ на MainFragment и работала кнопка обновить/синхронизировать.
     * Если пользователь например искал погоду в другом городе, то чтобы он мог не вписывать свой
     * город в поисковый диалог, а просто нажать кнопку обновить/синхронизировать и ему бы снова
     * показало погоду в месте, где он находится.
     */
    val latitude = MutableLiveData<String>()
    val longitude = MutableLiveData<String>()
    val userDeviceLanguage = MutableLiveData<String>()

    private val weatherApiMain: IWeatherApi = MyRetrofit.initRetrofit(MyConstants.WEATHER_API_BASE_URL)
    private val weatherApiGeocoding: IWeatherApi = MyRetrofit.initRetrofit(MyConstants.GEOCODING_API_BASE_URL)

    var job: Job? = null


    /** Функция запускает получение данных прогноза погоды как на текущий момент, так и прогноз на
     * 5 дней вперёд.
     */
    fun receiveWeatherData(dialog: AlertDialog?, latitude: String, longitude: String) {

        job = CoroutineScope(Dispatchers.Main).launch {

            val currentWeatherDataResponse = getCurrentWeatherData(latitude, longitude)
            val weatherForecastFor5DaysResponse = getWeatherDataForecastFor5Days(latitude, longitude)

            if (currentWeatherDataResponse.isSuccessful) {
                currentDayWeatherData.value = currentWeatherDataResponse.body()
            }

            if (weatherForecastFor5DaysResponse.isSuccessful) {
                fiveDaysWeatherData.value = weatherForecastFor5DaysResponse.body()
            }

            dialog?.cancel()
        }
    }


    suspend fun receiveWeatherInRequestedCity(dialog: AlertDialog, cityName: String) : Boolean = withContext(Dispatchers.IO) {

        /* Эта функция будет возвращать ответ true или false в зависимости от того, удалось ли
         * получить погоду в городе.
         * Если пользователь ввёл например не город, а просто набор букв, например: вававапваапвап,
         * или допустил ошибку в названии города, например: Londonn, то в блоке catch записываю в
         * эту переменную значение false и возвращаю его.
         * А в классе MainFragment проверяется, что если функция веернула false - я показываю
         * пользователю диалог о том, что он неверно ввёл город, попробуйте ещё раз.
         */
        var weatherInRequestedCityReceived = true

        val weatherInRequestedCityResponse = weatherApiGeocoding.getWeatherDataInRequestedCity(
            cityName,
            MyConstants.GEOCODING_API_LIMIT_PARAMETER, MyConstants.MY_WEATHER_API_KEY
        )

        if (weatherInRequestedCityResponse.isSuccessful) {

            /* Делаю здесь проверку на то, если пользователь вместо города в нужном формате
             * (например Rome, Minsk etc. ввёл какую-нибудь хрень типо: dffggfdgdfgdsfdsfsdgf.
             * Или неправильно ввёл город.
             * В этом случае почему-то response приходит successful, но не получается взять latitude
             * с 0 позиции т.к. её нет, потому что нет такого города.
             * И приходит IndexOutOfBoundException.
             */
            try {
                val latitude = weatherInRequestedCityResponse.body()?.get(0)?.lat.toString()
                val longitude = weatherInRequestedCityResponse.body()?.get(0)?.lon.toString()

                receiveWeatherData(dialog, latitude, longitude)

            } catch (error: IndexOutOfBoundsException) {
                weatherInRequestedCityReceived = false
                dialog.cancel()
            }
        }

        return@withContext weatherInRequestedCityReceived
    }


    private suspend fun getCurrentWeatherData(latitude: String, longitude: String) : Response<CurrentDayWeatherData> = withContext(Dispatchers.IO) {

        val weatherData = weatherApiMain.getCurrentWeatherData(
            latitude,
            longitude,
            MyConstants.MY_WEATHER_API_KEY,
            MyConstants.UNITS_OF_MEASUREMENT_METRIC,
            userDeviceLanguage.value!!
        )

        return@withContext weatherData
    }


    private suspend fun getWeatherDataForecastFor5Days(latitude: String, longitude: String) : Response<FiveDaysAndThreeHoursWeatherData> = withContext(Dispatchers.IO) {

        val weatherData = weatherApiMain.getForecastWeatherDataFor5Days(
            latitude,
            longitude,
            MyConstants.MY_WEATHER_API_KEY,
            MyConstants.UNITS_OF_MEASUREMENT_METRIC,
            userDeviceLanguage.value!!
        )

        return@withContext weatherData
    }


}