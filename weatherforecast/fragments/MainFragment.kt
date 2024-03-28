package com.nikita_zayanchkovskij.weatherforecast.fragments


import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.nikita_zayanchkovskij.weatherforecast.R
import com.nikita_zayanchkovskij.weatherforecast.adapters.DaysRcViewAdapter
import com.nikita_zayanchkovskij.weatherforecast.adapters.HoursRcViewAdapter
import com.nikita_zayanchkovskij.weatherforecast.databinding.MainFragmentBinding
import com.nikita_zayanchkovskij.weatherforecast.interfaces.IDialogButtonsListener
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.CurrentDayWeatherData
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.ListData
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.MyDaysForecastItem
import com.nikita_zayanchkovskij.weatherforecast.mvvm.viewmodel.MyViewModel
import com.nikita_zayanchkovskij.weatherforecast.utils.MyDialogs
import com.nikita_zayanchkovskij.weatherforecast.utils.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainFragment: Fragment() {
    private lateinit var binding: MainFragmentBinding
    private val myViewModel: MyViewModel by activityViewModels()

    private val hoursAdapter = HoursRcViewAdapter(this)
    private val daysAdapter = DaysRcViewAdapter(this)
    private val daysList = arrayListOf<MyDaysForecastItem>()

    private var currentTimeElementPosition = 0
    private var firstDayDateAndNextDaysDateToCompare = ""
    private var cityName = ""

    private lateinit var dayItem: MyDaysForecastItem
    private val dayMaxTempList = arrayListOf<Int>()
    private val dayMinTempList = arrayListOf<Int>()

    private var dayConditionAt12am = "" /* Например sunny, light rain и т.д. */
    private var dayIconAtCurrentTimeOrAt12amId = ""
    private lateinit var dialog: AlertDialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = MyDialogs.createProgressDialog(activity as AppCompatActivity)

        initHoursAndDaysRecyclerView()
        initBannerAdvertisement()
        receiveCurrentWeatherData()
        receiveHoursAndDaysWeatherData()
        buttonsSearchAndSync()
    }


    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }


    override fun onPause() {
        super.onPause()
        binding.adView.pause()
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.adView.destroy()
    }


    private fun initHoursAndDaysRecyclerView() = with(binding) {
        rcViewHours.layoutManager = LinearLayoutManager(
            activity as AppCompatActivity, LinearLayoutManager.HORIZONTAL, false)

        rcViewHours.adapter = hoursAdapter

        rcViewDays.layoutManager = LinearLayoutManager(activity as AppCompatActivity)
        rcViewDays.adapter = daysAdapter
    }


    private fun receiveCurrentWeatherData() {
        myViewModel.currentDayWeatherData.observe(viewLifecycleOwner) {
            fillInUserInterfaceMainCardView(it)
        }
    }


    private fun receiveHoursAndDaysWeatherData() {

        myViewModel.fiveDaysWeatherData.observe(viewLifecycleOwner) {

            /* Передаю subList с позиции от 0 до 9 т.к. хочу, чтобы мне показало только прогноз с
             * 12 часов например до 12 часов следующего дня, а если просто it.list передать - то
             * приходит прогноз по часам на 5 дней вперёд.
             */
            hoursAdapter.submitList(it.list.subList(0, 9))

            /* Делаю в корутине и делаю функцию fillInDaysList() suspend т.к. мне нужно сначала
             * закончить заполнение списка дней для отображения, и только потом уже передавать этот
             * список в адаптер. Нельзя, чтобы раньше заполнение адаптера запустилось.
             */
            CoroutineScope(Dispatchers.Main).launch {
                daysList.clear()
                fillInDaysList(it.city.name, it.list)
                daysAdapter.submitList(ArrayList(daysList))
                dialog.cancel()
              //daysAdapter.notifyDataSetChanged()
            }
        }
    }


    private suspend fun fillInDaysList(city: String, itemsList: List<ListData>) = withContext(Dispatchers.IO) {
        currentTimeElementPosition = 0
        cityName = city
        dayMaxTempList.clear()
        dayMinTempList.clear()

        /* Из полной даты 2024-08-10 12:00:00 оставляю только день: 10, 11 и т.д.
         * Т.к. я хочу показать прогноз погоды по дням буду сравнивать день, если пришло число больше
         * - значит это следующий день. Сначала беру дату первого элементы из списка.
         */
        firstDayDateAndNextDaysDateToCompare = itemsList[0].dt_txt.substring(7, 10)
        dayIconAtCurrentTimeOrAt12amId = itemsList[0].weather[0].icon
        dayConditionAt12am = itemsList[0].weather[0].description

        /* Прохожусь по всем дням, которые получил, точнее по каждым 3ём часам каждого дня, т.к.
         * в таком виде приходит с сервера.
         */
        itemsList.forEach { dateAndTimeItem ->

            if (daysList.size == 5) return@withContext

            checkDate(
                itemsList,
                itemsList[currentTimeElementPosition].dt_txt.substring(7, 10),
                currentTimeElementPosition
            ) //проверяю

            currentTimeElementPosition++ //увеличиваю
        }
    }


    private fun checkDate(itemsList: List<ListData>, itemDateOrTime: String, currentTimeElementPosition: Int) {

        if (itemDateOrTime == firstDayDateAndNextDaysDateToCompare) {
            fillInDaysItemInformation(itemsList)

        } else {
            firstDayDateAndNextDaysDateToCompare = itemsList[currentTimeElementPosition].dt_txt.substring(7, 10)
            dayMaxTempList.clear()
            dayMinTempList.clear()
            dayConditionAt12am = ""
            dayIconAtCurrentTimeOrAt12amId = ""

            fillInDaysItemInformation(itemsList)
        }
    }


    private fun fillInDaysItemInformation(itemsList: List<ListData>) {
        dayMaxTempList.add(itemsList[currentTimeElementPosition].main.temp_max.toInt())
        dayMinTempList.add(itemsList[currentTimeElementPosition].main.temp_min.toInt())

        /* Здесь проверяю, если дошли до времени 12:00, то беру WeatherCondition
         * (например sunny, light rain и т.д.) по состоянию этого дня на 12 часов и иконку
         * погоды на 12 часов и сохраняю их для отображения на экране.
         */
        if (itemsList[currentTimeElementPosition].dt_txt.substring(11, 16) == "12:00") {

            /* Делаю weather[0] т.к. с сервера weather приходит в виде списка, но там только
             * одна позиция, поэтому таким образом.
             */
            dayConditionAt12am = itemsList[currentTimeElementPosition].weather[0].description
            dayIconAtCurrentTimeOrAt12amId = itemsList[currentTimeElementPosition].weather[0].icon
        }

        /* Когда получил все данные на целый день до вечера - можно создавать item и
         * добавлять в список.
         */
        if (itemsList[currentTimeElementPosition].dt_txt.substring(11, 16) == "21:00") {
            val dayIconAt12amUrl = "https://openweathermap.org/img/wn/$dayIconAtCurrentTimeOrAt12amId@2x.png"

            dayItem = MyDaysForecastItem (
                cityName,
                itemsList[currentTimeElementPosition].dt_txt.substring(0, 10),
                dayConditionAt12am,
                dayMaxTempList.max().toString(), dayMinTempList.min().toString(),
                dayIconAt12amUrl
            )

            daysList.add(dayItem)
        }
    }


    /** Функция заполняет главный CardView на экране данными погоды.
     */
    private fun fillInUserInterfaceMainCardView(currentDayWeatherData: CurrentDayWeatherData) = with(binding) {
        val dateAndTimeOfUpdate = TimeFormatter.convertTimeInMillisToDateAndTime(currentDayWeatherData.dt)

        val iconId = currentDayWeatherData.weather[0].icon
        val iconUrl = "https://openweathermap.org/img/wn/$iconId@2x.png"

        val countryAndCity = "${currentDayWeatherData.sys.country}, ${currentDayWeatherData.name}"
        val sunRiseTime = TimeFormatter.convertTimeInMillisToTimeOnly(currentDayWeatherData.sys.sunrise)
        val sunSetTime = TimeFormatter.convertTimeInMillisToTimeOnly(currentDayWeatherData.sys.sunset)

        /* Делаю таким образом т.к. если без этого, то показывает состояние например "overcast clouds",
         * т.к. таким образом строка приходит с WeatherApi, а я хочу, чтобы начинался текст с большой
         * буквы - поэтому делаю таким образом.
         */
        val conditionFirstLetterInUpperCase = currentDayWeatherData.weather[0].description.substring(0, 1).uppercase()
        val conditionRestOfTheText = currentDayWeatherData.weather[0].description.substring(1)
        val conditionString = "$conditionFirstLetterInUpperCase$conditionRestOfTheText"

        tvDateAndTime.text = dateAndTimeOfUpdate

        Glide
            .with(activity as AppCompatActivity)
            .load(iconUrl)
            .placeholder(R.drawable.ic_image_place_holder)
            .into(imIconWeatherCondition)

        tvCountryAndCity.text = countryAndCity
        tvCurrentTempData.text = currentDayWeatherData.main.temp.toInt().toString()
        tvCondition.text = conditionString
        tvSunriseData.text = sunRiseTime
        tvSunsetData.text = sunSetTime
        tvFeelsLikeData.text = currentDayWeatherData.main.feels_like.toInt().toString()
        tvHumidityData.text = currentDayWeatherData.main.humidity.toString()
        tvWindSpeedData.text = currentDayWeatherData.wind.speed.toInt().toString()

        val kmH = "(${convertMetersPerSecondToKmH(currentDayWeatherData.wind.speed)}"
        tvWindSpeedKmHData.text = kmH


        tvPressureData.text = currentDayWeatherData.main.pressure.toString()

        val mmHgPressure = "(${convertAtmosphericPressureFromHPAtoMMHG(currentDayWeatherData.main.pressure)}"
        tvPressureMmHgData.text = mmHgPressure
    }


    private fun buttonsSearchAndSync() = with(binding) {

        imbtSearch.setOnClickListener {

            MyDialogs.searchWeatherInRequestedCityDialog(activity as AppCompatActivity, object: IDialogButtonsListener {

                override fun positiveButtonPressed(cityName: String?) {

                    if (cityName?.isNotEmpty() == true) {

                        CoroutineScope(Dispatchers.Main).launch {

                            val weatherReceived = myViewModel.receiveWeatherInRequestedCity(
                                    dialog = MyDialogs.createProgressDialog(activity as AppCompatActivity),
                                    cityName)

                            if (weatherReceived == false) {
                                MyDialogs
                                    .createNotifyDialog(activity as AppCompatActivity,
                                        getString(R.string.dialog_attention),
                                        getString(R.string.dialog_incorrect_city_name))
                            }
                        }

                    /* Если пользователь ничего не ввёл и пытается сделать поиск - показываю ему
                     * сообщение об этом.
                     */
                    } else {
                        MyDialogs.createNotifyDialog(
                            requireContext(),
                            getString(R.string.dialog_attention),
                            getString(R.string.dialog_city_is_empty_message)
                        )
                    }
                }

                override fun negativeButtonPressed() {}
            })
        }

        imbtSync.setOnClickListener {
            myViewModel
                .receiveWeatherData(
                    dialog = MyDialogs.createProgressDialog(activity as AppCompatActivity),
                    myViewModel.latitude.value!!, myViewModel.longitude.value!!)
        }
    }


    /** Функция конвертирует атмосферное давление из hPa в mmHg (миллиметры ртутного столба).
     */
    private fun convertAtmosphericPressureFromHPAtoMMHG(pressure: Int): String {
        val mmHg = pressure * (7.50062 * 0.001)
        return  mmHg.toInt().toString()
    }


    private fun convertMetersPerSecondToKmH(metersPerSecond: Float): String {
        val kmH = metersPerSecond * 3.6
        return  kmH.toInt().toString()
    }


    /** Инициализирую баннерную рекламу.
     */
    private fun initBannerAdvertisement() {
        MobileAds.initialize(activity as AppCompatActivity)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }


    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }


}