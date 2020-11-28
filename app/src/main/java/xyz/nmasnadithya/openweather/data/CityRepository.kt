package xyz.nmasnadithya.openweather.data

import xyz.nmasnadithya.openweather.data.models.entity.City
import xyz.nmasnadithya.openweather.utils.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single


interface CityRepository {
  /**
   * Change selected city to [city]
   * @param city
   * @return a [Completable], emit [SaveSelectedCityError] when error
   */
  fun changeSelectedCity(city: City): Completable

  /**
   * Add city by [latitude] and [longitude]
   * @param latitude
   * @param longitude
   * @return a [Single] emit added city or emit error
   */
  fun addCityByLatLng(latitude: Double, longitude: Double): Single<City>

  /**
   * Delete [city]
   * @param city
   * @return a [Single] of deleted city
   */
  fun deleteCity(city: City): Single<City>

  /**
   * Get stream of selected city
   * @return [Observable] emit None when having no selected city, otherwise emit Some of [City]
   */
  fun getSelectedCity(): Observable<Optional<City>>

  /**
   * Synchronous access  selected city
   */
  val selectedCity: City?
}