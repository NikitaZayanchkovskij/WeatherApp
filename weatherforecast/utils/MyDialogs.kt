package com.nikita_zayanchkovskij.weatherforecast.utils


import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.nikita_zayanchkovskij.weatherforecast.R
import com.nikita_zayanchkovskij.weatherforecast.databinding.ProgressDialogLayoutBinding
import com.nikita_zayanchkovskij.weatherforecast.databinding.SearchWeatherInCityDialogBinding
import com.nikita_zayanchkovskij.weatherforecast.interfaces.IDialogButtonsListener


object MyDialogs {


    /** Этим диалогом буду пользоваться только в MainActivity т.к. хочу, чтобы при нажатии на кнопку
     * ОК закрылось приложение т.к. нет разрешения на использование местоположения.
     */
    fun createLocationPermissionDialog(context: AppCompatActivity, title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()

        dialog.setTitle(title) /* Название диалога, например Внимание! */
        dialog.setMessage(message) /* Сообщение диалога (сообщение пользователю). */

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.dialog_ok)) { _,_ ->
            dialog.dismiss()
            context.finish()
        }

        dialog.show()
    }


    fun createNotifyDialog(context: Context, title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()

        dialog.setTitle(title) /* Название диалога, например Внимание! */
        dialog.setMessage(message) /* Сообщение диалога (сообщение пользователю). */

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.dialog_ok)) { _,_ ->
            dialog.dismiss()
        }

        dialog.show()
    }


    fun createEnableGpsDialog(context: Context, listener: IDialogButtonsListener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()

        dialog.setTitle(context.getString(R.string.dialog_attention))
        dialog.setMessage(context.getString(R.string.dialog_enable_gps_question))

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.dialog_ok)) { _,_ ->
            listener.positiveButtonPressed(null)
            dialog.dismiss()
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.dialog_cancel)) { _,_ ->
            listener.negativeButtonPressed()
            dialog.dismiss()
        }

        dialog.show()
    }


    fun searchWeatherInRequestedCityDialog(act: AppCompatActivity, listener: IDialogButtonsListener) {
        val builder = AlertDialog.Builder(act)
        val binding = SearchWeatherInCityDialogBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)
        val dialog = builder.create()

        binding.btSearch.setOnClickListener {
            listener.positiveButtonPressed(binding.edSearchedCity.text.toString())
            dialog.dismiss()
        }

        binding.btCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    fun createProgressDialog(act: AppCompatActivity): AlertDialog {
        val builder = AlertDialog.Builder(act)
        val binding = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)

        val dialog = builder.create()

        dialog.setCancelable(false)

        dialog.show()

        return dialog
    }


}