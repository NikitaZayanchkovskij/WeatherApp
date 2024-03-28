package com.nikita_zayanchkovskij.weatherforecast.internet_connection_manager


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import com.nikita_zayanchkovskij.weatherforecast.MainActivity


/** Этот класс проверяет, включен ли интернет у пользователя на телефоне.
 */
class InternetConnectionManager(context: MainActivity): LiveData<Boolean>() {
    private val connectionManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    override fun onActive() {
        super.onActive()
        checkInternetConnection()
    }


    override fun onInactive() {
        super.onInactive()
        connectionManager.unregisterNetworkCallback(networkCallback)
    }


    private val networkCallback = object: ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            postValue(false)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }
    }


    private fun checkInternetConnection() {
        val network = connectionManager.activeNetwork

        if (network == null) {
            postValue(false)
        }

        val requestBuilder = NetworkRequest.Builder().apply {
            addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        }.build()

        connectionManager.registerNetworkCallback(requestBuilder, networkCallback)
    }


}