package com.nikita_zayanchkovskij.weatherforecast.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nikita_zayanchkovskij.weatherforecast.R
import com.nikita_zayanchkovskij.weatherforecast.databinding.DaysRcviewItemBinding
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.MyDaysForecastItem
import java.text.SimpleDateFormat
import java.util.Locale


class DaysRcViewAdapter(private val context: Fragment):
    ListAdapter<MyDaysForecastItem, DaysRcViewAdapter.WeatherHolder>(WeatherComparator()) {


    class WeatherHolder(view: View, private val context: Fragment): RecyclerView.ViewHolder(view) {
        private val binding = DaysRcviewItemBinding.bind(view)

        fun setData(item: MyDaysForecastItem) = with(binding) {
            val dateInNeededFormat = convertDate(item)
            val dayOfTheWeek = convertDateToDayOfTheWeek(item)

            tvDateAndTime.text = dateInNeededFormat
            tvDayOfTheWeek.text = dayOfTheWeek.uppercase()
            tvCondition.text = item.weatherConditionDescription
            tvMaxTemp.text = item.maxTemp
            tvMinTemp.text = item.minTemp

            Glide
                .with(context)
                .load(item.icon)
                .placeholder(R.drawable.ic_image_place_holder)
                .into(imCondition)
        }

        private fun convertDateToDayOfTheWeek(item: MyDaysForecastItem): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(item.dtTxtButOnlyDate)
            sdf.applyPattern("EEE")
            val dayOfTheWeek = sdf.format(date!!)

            return dayOfTheWeek
        }


        private fun convertDate(item: MyDaysForecastItem): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(item.dtTxtButOnlyDate)
            sdf.applyPattern("dd.MM.yyyy")
            val dateToReturn = sdf.format(date!!)
            return dateToReturn
        }
    }


    class WeatherComparator: DiffUtil.ItemCallback<MyDaysForecastItem>() {

        override fun areItemsTheSame(oldItem: MyDaysForecastItem, newItem: MyDaysForecastItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MyDaysForecastItem, newItem: MyDaysForecastItem): Boolean {
            return oldItem == newItem
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherHolder {
        val view = LayoutInflater
            .from(parent.context).inflate(R.layout.days_rcview_item, parent, false)

        return WeatherHolder(view, context)
    }


    override fun onBindViewHolder(holder: WeatherHolder, position: Int) {
        holder.setData(getItem(position))
    }


}