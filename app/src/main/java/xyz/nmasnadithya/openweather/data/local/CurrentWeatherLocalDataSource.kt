package xyz.nmasnadithya.openweather.data.local

import xyz.nmasnadithya.openweather.data.models.entity.CityAndCurrentWeather
import xyz.nmasnadithya.openweather.data.models.entity.CurrentWeather
import io.reactivex.Completable
import io.reactivex.Observable
import xyz.nmasnadithya.openweather.data.local.CurrentWeatherDao

/**
 * A wrapper of [CurrentWeatherDao]
 */

class CurrentWeatherLocalDataSource(private val currentWeatherDao: CurrentWeatherDao) {
  fun getCityAndCurrentWeatherByCityId(cityId: Long): Observable<CityAndCurrentWeather> {
    return currentWeatherDao
        .getCityAndCurrentWeatherByCityId(cityId)
        .distinctUntilChanged()
  }

  fun getAllCityAndCurrentWeathers(querySearch: String): Observable<List<CityAndCurrentWeather>> {
    return currentWeatherDao
        .getAllCityAndCurrentWeathers(querySearch)
        .distinctUntilChanged()
  }

  fun insertOrUpdateCurrentWeather(weather: CurrentWeather): Completable {
    return Completable.fromAction {
      currentWeatherDao.upsert(weather)
    }
  }
}