package xyz.nmasnadithya.openweather.ui.main.fivedayforecast

import android.os.Parcelable
import xyz.nmasnadithya.openweather.data.models.WindDirection
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime

sealed class DailyWeatherListItem {
  @Parcelize
  data class Weather(
      val colors: Pair<Int, Int>,
      val weatherIcon: String,
      val dataTime: ZonedDateTime,
      val weatherDescription: String,
      val temperatureMin: String,
      val temperatureMax: String,
      val temperature: String,
      val pressure: String,
      val seaLevel: String,
      val groundLevel: String,
      val humidity: String,
      val main: String,
      val cloudiness: String,
      val winSpeed: String,
      val windDirection: WindDirection,
      val rainVolumeForTheLast3Hours: String,
      val snowVolumeForTheLast3Hours: String
  ) : DailyWeatherListItem(), Parcelable

  data class Header(val date: ZonedDateTime) : DailyWeatherListItem()
}
