package xyz.nmasnadithya.openweather.data

import xyz.nmasnadithya.openweather.data.local.FiveDayForecastLocalDataSource
import xyz.nmasnadithya.openweather.data.local.SelectedCityPreference
import xyz.nmasnadithya.openweather.data.remote.OpenWeatherMapApiService
import xyz.nmasnadithya.openweather.utils.None
import xyz.nmasnadithya.openweather.utils.Some
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class FiveDayForecastRepositoryImpl(
    private val openWeatherMapApiService: OpenWeatherMapApiService,
    private val fiveDayForecastLocalDataSource: FiveDayForecastLocalDataSource,
    private val selectedCityPreference: SelectedCityPreference
) : FiveDayForecastRepository {
  private val fiveDayForecastObservable = selectedCityPreference
      .observable
      .distinctUntilChanged()
      .switchMap { optional ->
        when (optional) {
          is Some -> fiveDayForecastLocalDataSource
              .getAllDailyWeathersByCityId(optional.value.id)
              .subscribeOn(Schedulers.io())
              .map { optional.value to it }
              .map(::Some)
          is None -> Observable.just(None)
        }
      }
      .replay(1)
      .autoConnect(0)
  private val refreshFiveDayForecast = Single
      .fromCallable { selectedCityPreference.value }
      .flatMap { cityOptional ->
        when (cityOptional) {
          is None -> Single.error(NoSelectedCityException)
          is Some -> openWeatherMapApiService
              .get5DayEvery3HourForecastByCityId(cityOptional.value.id)
              .subscribeOn(Schedulers.io())
              .flatMap {
                LocalDataSourceUtil.saveFiveDayForecastWeather(
                    fiveDayForecastLocalDataSource,
                    it
                )
              }
              .map { cityOptional.value to it }
        }
      }

  override fun getFiveDayForecastOfSelectedCity() = fiveDayForecastObservable

  override fun refreshFiveDayForecastOfSelectedCity() = refreshFiveDayForecast
}