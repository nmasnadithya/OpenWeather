package xyz.nmasnadithya.openweather.data.remote

import xyz.nmasnadithya.openweather.data.models.apiresponse.timezonedb.TimezoneDbResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL_TIMEZONE_DB = "https://api.timezonedb.com/v2.1/"

interface TimezoneDbApiService {
  @GET("get-time-zone")
  fun getTimezoneByLatLng(
      @Query("lat") lat: Double,
      @Query("lng") lng: Double
  ): Single<TimezoneDbResponse>
}

fun getZoneId(
    timezoneDbApiService: TimezoneDbApiService,
    latitude: Double,
    longitude: Double
): Single<String> {
  return timezoneDbApiService
      .getTimezoneByLatLng(latitude, longitude)
      .subscribeOn(Schedulers.io())
      .map {
        if (it.status != "OK") {
          ""
        } else {
          it.zoneName
        }
      }
      .onErrorReturnItem("")
}
