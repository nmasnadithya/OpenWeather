package xyz.nmasnadithya.openweather.data.local

import xyz.nmasnadithya.openweather.data.models.entity.City
import io.reactivex.Completable
import xyz.nmasnadithya.openweather.data.local.CityDao

/**
 * A wrapper of [CityDao]
 */

class CityLocalDataSource(private val cityDao: CityDao) {
  fun deleteCity(city: City): Completable {
    return Completable.fromAction {
      cityDao.deleteCity(city)
    }
  }

  fun insertOrUpdateCity(city: City): Completable {
    return Completable.fromAction {
      cityDao.upsert(city)
    }
  }
}