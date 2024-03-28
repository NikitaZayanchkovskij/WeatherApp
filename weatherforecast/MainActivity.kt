package com.nikita_zayanchkovskij.weatherforecast


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.nikita_zayanchkovskij.weatherforecast.databinding.ActivityMainBinding
import com.nikita_zayanchkovskij.weatherforecast.fragments.MainFragment
import com.nikita_zayanchkovskij.weatherforecast.interfaces.IDialogButtonsListener
import com.nikita_zayanchkovskij.weatherforecast.internet_connection_manager.InternetConnectionManager
import com.nikita_zayanchkovskij.weatherforecast.mvvm.viewmodel.MyViewModel
import com.nikita_zayanchkovskij.weatherforecast.utils.MyDialogs
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var networkManager: InternetConnectionManager
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val myViewModel: MyViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myViewModel.userDeviceLanguage.value = Locale.getDefault().language
        checkLocationPermission()
    }


    override fun onResume() {
        super.onResume()

        /* Без этой проверки если пользователь отклоняет разрешение на использования местоположения,
         * то всё равно спрашивает включить ли GPS - так не должно быть т.к. если нет разрешения -
         * приложенияем пользоваться нельзя.
         */
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) == true) {
            checkLocation()
        }
    }


    override fun onStop() {
        super.onStop()
        myViewModel.job?.cancel()
    }


    /** Функция проверяет, дал ли пользователь разрешение на использование местоположения.
     * 0 - это значит есть разрешение, а -1 нет разрешения.
     */
    private fun checkLocationPermission() {

        /* Если НЕ предоставлено разрешение - запрашиваем его. */
        if (! isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationPermissionDialogLauncher()
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    /** Функция будет возвращать ответ, предоставлено ли разрешение на использование местоположения.
     */
    private fun isPermissionGranted(permissionTitle: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionTitle) == PackageManager.PERMISSION_GRANTED
    }


    /** Это диалог, который спрашивает разрешение на использование местоположения.
     */
    private fun locationPermissionDialogLauncher() {
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            /* Если нет разрешения - показываю сообщение, что нужно разрешить. */
            if (it == false) {
                MyDialogs.createLocationPermissionDialog(this,
                    getString(R.string.dialog_attention),
                    getString(R.string.dialog_no_location_permission_message))
            }
        }
    }


    /** Если GPS включен на телефоне - получаем текущее местоположение пользователя.
     * Если нет - показываем диалог о том, что нужно включить GPS.
     * При нажатии на кнопку Да диалога - открывается окно настроек GPS.
     */
    private fun checkLocation() {

        if (checkIfGpsIsEnabled() == true) {
            checkInternetConnection()

        } else {

            MyDialogs.createEnableGpsDialog(this, object : IDialogButtonsListener {

                override fun positiveButtonPressed(cityNameAndCountryCode: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

                override fun negativeButtonPressed() {
                    MyDialogs.createNotifyDialog(this@MainActivity,
                        getString(R.string.dialog_attention),
                        getString(R.string.dialog_disabled_gps_message))
                }
            })
        }
    }


    private fun checkIfGpsIsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    /** Функция проверяет, есть ли подключение к интернету.
     * Если есть - можно получать данные (погоду) с сервера.
     * Если нет - показываем иконки (заглушку) об этом и прячем всё остальное.
     */
    private fun checkInternetConnection() = with(binding) {
        networkManager = InternetConnectionManager(this@MainActivity)

        networkManager.observe(this@MainActivity) { internet ->

            if (internet == true) {
                noInternetLayout.visibility = View.GONE
                openMainFragment()
                receiveUserLocation()

            } else {
                noInternetLayout.visibility = View.VISIBLE
            }
        }
    }


    private fun receiveUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val cancellationToken = CancellationTokenSource()

        /* Если нужных разрешений нет - ничего не делаю, просто return.
         * Если есть - получаю местоположение пользователя.
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            return
        }

        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnCompleteListener {

                /* Передаю местоположение пользователя для запроса погоды в его местоположении.
                 * А также сохраняю координаты пользователя во ViewModel, чтобы на MainFragment к
                 * ним был доступ и работала кнопка обновить/синхронизировать.
                 */
                myViewModel.latitude.value = it.result.latitude.toString()
                myViewModel.longitude.value = it.result.longitude.toString()

                myViewModel.receiveWeatherData(null,
                    it.result.latitude.toString(), it.result.longitude.toString()
                )
            }
    }


    private fun openMainFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentHolder, MainFragment.newInstance())
            .commit()
    }


}