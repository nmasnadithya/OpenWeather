package xyz.nmasnadithya.openweather.koin

import xyz.nmasnadithya.openweather.ui.addcity.AddCityPresenter
import xyz.nmasnadithya.openweather.ui.cities.CitiesPresenter
import xyz.nmasnadithya.openweather.ui.main.ColorHolderSource
import xyz.nmasnadithya.openweather.ui.main.MainActivity
import xyz.nmasnadithya.openweather.ui.main.MainPresenter
import xyz.nmasnadithya.openweather.ui.main.chart.ChartPresenter
import xyz.nmasnadithya.openweather.ui.main.currentweather.CurrentWeatherPresenter
import xyz.nmasnadithya.openweather.ui.main.fivedayforecast.DailyWeatherPresenter
import org.koin.android.ext.koin.androidApplication
import org.koin.core.scope.Scope
import org.koin.dsl.module
import timber.log.Timber

@ExperimentalStdlibApi
val presenterModule = module {
  factory { getCitiesPresenter() }

  factory { getCurrentWeatherPresenter() }

  factory { getAddCityPresenter() }

  factory { getChartPresenter() }

  scope<MainActivity> {
    scoped { getColorHolderSource() }

    factory { getMainPresenter() }

    factory { getDailyWeatherPresenter() }
  }
}

private fun Scope.getColorHolderSource() = ColorHolderSource(androidApplication())

private fun Scope.getChartPresenter(): ChartPresenter {
  return ChartPresenter(get(), get())
}

private fun Scope.getMainPresenter(): MainPresenter {
  val colorHolderSource = get<ColorHolderSource>()
  Timber.d("Create MainPresenter with $colorHolderSource")
  return MainPresenter(get(), colorHolderSource, androidApplication())
}

@ExperimentalStdlibApi
private fun Scope.getDailyWeatherPresenter(): DailyWeatherPresenter {
  val colorHolderSource = get<ColorHolderSource>()
  Timber.d("Create DailyWeatherPresenter with $colorHolderSource")
  return DailyWeatherPresenter(get(), get(), get(), colorHolderSource, androidApplication())
}

private fun Scope.getAddCityPresenter(): AddCityPresenter {
  return AddCityPresenter(get(), get(), androidApplication())
}

@ExperimentalStdlibApi
private fun Scope.getCurrentWeatherPresenter(): CurrentWeatherPresenter {
  return CurrentWeatherPresenter(get(), get(), androidApplication(), get())
}

@ExperimentalStdlibApi
private fun Scope.getCitiesPresenter(): CitiesPresenter {
  return CitiesPresenter(get(), get(), get(), get(), androidApplication())
}