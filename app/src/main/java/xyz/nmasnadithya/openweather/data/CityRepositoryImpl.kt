package xyz.nmasnadithya.openweather.data

import xyz.nmasnadithya.openweather.data.LocalDataSourceUtil.saveCityAndCurrentWeather
import xyz.nmasnadithya.openweather.data.LocalDataSourceUtil.saveFiveDayForecastWeather
import xyz.nmasnadithya.openweather.data.local.CityLocalDataSource
import xyz.nmasnadithya.openweather.data.local.CurrentWeatherLocalDataSource
import xyz.nmasnadithya.openweather.data.local.FiveDayForecastLocalDataSource
import xyz.nmasnadithya.openweather.data.local.SelectedCityPreference
import xyz.nmasnadithya.openweather.data.models.entity.City
import xyz.nmasnadithya.openweather.data.remote.OpenWeatherMapApiService
import xyz.nmasnadithya.openweather.data.remote.TimezoneDbApiService
import xyz.nmasnadithya.openweather.data.remote.getZoneId
import xyz.nmasnadithya.openweather.utils.None
import xyz.nmasnadithya.openweather.utils.Optional
import xyz.nmasnadithya.openweather.utils.Some
import xyz.nmasnadithya.openweather.utils.getOrNull
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class CityRepositoryImpl(
    private val openWeatherMapApiService: OpenWeatherMapApiService,
    private val timezoneDbApiService: TimezoneDbApiService,
    private val cityLocalDataSource: CityLocalDataSource,
    private val fiveDayForecastLocalDataSource: FiveDayForecastLocalDataSource,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val selectedCityPreference: SelectedCityPreference
) : CityRepository {

  override val selectedCity get() = selectedCityPreference.value.getOrNull()

  override fun getSelectedCity() = selectedCityPreference.observable

  override fun deleteCity(city: City): Single<City> {
    return Completable.mergeArray(
        cityLocalDataSource
            .deleteCity(city)
            .subscribeOn(Schedulers.io()),
        /**
         * If [city] is current selected city, then [changeSelectedCity] to null
         */
        Single
            .fromCallable { selectedCityPreference.value }
            .filter { it.getOrNull() == city }
            .flatMapCompletable { changeSelectedCity(None) }
    ).toSingleDefault(city)
  }

  override fun addCityByLatLng(latitude: Double, longitude: Double): Single<City> {
    return Singles.zip(
        openWeatherMapApiService
            .getCurrentWeatherByLatLng(latitude, longitude)
            .subscribeOn(Schedulers.io()),
        getZoneId(timezoneDbApiService, latitude, longitude)
    )
        .flatMap {
          Timber.d("City Found: $it")
          saveCityAndCurrentWeather(
              cityLocalDataSource,
              currentWeatherLocalDataSource,
              it.first,
              it.second
          )
        }
        .map { it.city }
        .flatMap { city ->
          openWeatherMapApiService
              .get5DayEvery3HourForecastByCityId(city.id)
              .subscribeOn(Schedulers.io())
              .flatMap { saveFiveDayForecastWeather(fiveDayForecastLocalDataSource, it) }
              .map { city }
        }
  }

  override fun changeSelectedCity(city: City) = changeSelectedCity(Some(city))

  private fun changeSelectedCity(optionalCity: Optional<City>): Completable {
    return Completable
        .fromCallable { selectedCityPreference.save(optionalCity) }
        .subscribeOn(Schedulers.single())
        .onErrorResumeNext { Completable.error(SaveSelectedCityError(it)) }
  }
}