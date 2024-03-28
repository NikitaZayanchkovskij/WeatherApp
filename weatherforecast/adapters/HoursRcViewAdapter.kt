package com.nikita_zayanchkovskij.weatherforecast.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nikita_zayanchkovskij.weatherforecast.R
import com.nikita_zayanchkovskij.weatherforecast.databinding.HoursRcviewItemBinding
import com.nikita_zayanchkovskij.weatherforecast.mvvm.model.ListData


class HoursRcViewAdapter(private val context: Fragment):
    ListAdapter<ListData, HoursRcViewAdapter.WeatherHolder>(WeatherComparator()) {


    class WeatherHolder(view: View, private val context: Fragment): RecyclerView.ViewHolder(view) {
        private val binding = HoursRcviewItemBinding.bind(view)

        fun setData(item: ListData) = with(binding) {
            var position = 0
            val onlyHoursAndMinutes = item.dt_txt.substring(11, 16)

            tvTime.text = onlyHoursAndMinutes
            tvTemp.text = item.main.temp.toInt().toString()

            val iconId = item.weather[position].icon
            val iconUrl = "https://openweathermap.org/img/wn/$iconId@2x.png"

            Glide
                .with(context)
                .load(iconUrl)
                .placeholder(R.drawable.ic_image_place_holder)
                .into(imCondition)

            position++
        }
    }


    class WeatherComparator: DiffUtil.ItemCallback<ListData>() {

        override fun areItemsTheSame(oldItem: ListData, newItem: ListData): Boolean {
            return oldItem.main.temp == newItem.main.temp
        }

        override fun areContentsTheSame(oldItem: ListData, newItem: ListData): Boolean {
            return oldItem == newItem
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherHolder {
        val view = LayoutInflater
            .from(parent.context).inflate(R.layout.hours_rcview_item, parent, false)

        return WeatherHolder(view, context)
    }


    override fun onBindViewHolder(holder: WeatherHolder, position: Int) {
        holder.setData(getItem(position))
    }


    override fun getItemCount(): Int {

        return super.getItemCount()
    }


}