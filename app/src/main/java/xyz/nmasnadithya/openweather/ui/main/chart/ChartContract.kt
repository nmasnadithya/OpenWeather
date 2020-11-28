package xyz.nmasnadithya.openweather.ui.main.chart

import com.hannesdorfmann.mosby3.mvp.MvpView
import xyz.nmasnadithya.openweather.data.models.PressureUnit
import xyz.nmasnadithya.openweather.data.models.SpeedUnit
import xyz.nmasnadithya.openweather.data.models.TemperatureUnit
import xyz.nmasnadithya.openweather.data.models.entity.DailyWeather

interface ChartContract {
  data class ViewState(
      val weathers: List<DailyWeather>,
      val temperatureUnit: TemperatureUnit,
      val pressureUnit: PressureUnit,
      val speedUnit: SpeedUnit
  )

  interface View : MvpView {
    fun render(viewState: ViewState)
  }
}