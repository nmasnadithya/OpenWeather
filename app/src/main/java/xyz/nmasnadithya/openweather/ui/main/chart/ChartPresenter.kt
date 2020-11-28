package xyz.nmasnadithya.openweather.ui.main.chart

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import xyz.nmasnadithya.openweather.data.FiveDayForecastRepository
import xyz.nmasnadithya.openweather.data.local.SettingPreferences
import xyz.nmasnadithya.openweather.data.models.PressureUnit
import xyz.nmasnadithya.openweather.data.models.SpeedUnit
import xyz.nmasnadithya.openweather.data.models.TemperatureUnit
import xyz.nmasnadithya.openweather.data.models.entity.DailyWeather
import xyz.nmasnadithya.openweather.ui.main.chart.ChartContract.View
import xyz.nmasnadithya.openweather.ui.main.chart.ChartContract.ViewState
import xyz.nmasnadithya.openweather.utils.getOrNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables.combineLatest
import timber.log.Timber

class ChartPresenter(
    fiveDayForecastRepository: FiveDayForecastRepository,
    settingPreferences: SettingPreferences
) : MviBasePresenter<View, ViewState>() {
  private val viewState = combineLatest(
      source1 = fiveDayForecastRepository.getFiveDayForecastOfSelectedCity(),
      source2 = settingPreferences.temperatureUnitPreference.observable,
      source3 = settingPreferences.speedUnitPreference.observable,
      source4 = settingPreferences.pressureUnitPreference.observable,
      combineFunction = { optional, temperatureUnit, speedUnit, pressureUnit ->
        Tuple4(
            temperatureUnit = temperatureUnit,
            weathers = optional.getOrNull()?.second.orEmpty(),
            pressureUnit = pressureUnit,
            speedUnit = speedUnit
        )
      })
      .map {
        ViewState(
            temperatureUnit = it.temperatureUnit,
            weathers = it.weathers,
            speedUnit = it.speedUnit,
            pressureUnit = it.pressureUnit
        )
      }
      .distinctUntilChanged()
      .doOnNext { Timber.d("Changing ViewState $it") }
      .observeOn(AndroidSchedulers.mainThread())!!

  override fun bindIntents() = subscribeViewState(viewState, View::render)

  private companion object {

    private data class Tuple4(
        val weathers: List<DailyWeather>,
        val temperatureUnit: TemperatureUnit,
        val speedUnit: SpeedUnit,
        val pressureUnit: PressureUnit
    )
  }
}