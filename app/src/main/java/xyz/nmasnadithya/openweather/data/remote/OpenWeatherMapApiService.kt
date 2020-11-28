package xyz.nmasnadithya.openweather.data.remote

import xyz.nmasnadithya.openweather.data.models.apiresponse.currentweatherapiresponse.CurrentWeatherResponse
import xyz.nmasnadithya.openweather.data.models.apiresponse.forecastweatherapiresponse.FiveDayForecastResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

const val OPEN_WEATHER_MAP_BASE_URL = "https://api.openweathermap.org/data/2.5/"
const val OPEN_WEATHER_MAP_APP_ID = "6592d24a33ae13c2ac1401db99732c61"

interface OpenWeatherMapApiService {
  @GET("weather")
  fun getCurrentWeatherByLatLng(
      @Query("lat") lat: Double,
      @Query("lon") lon: Double
  ): Single<CurrentWeatherResponse>

  @GET("weather")
  fun getCurrentWeatherByCityId(
      @Query("id") id: Long
  ): Single<CurrentWeatherResponse>

  @GET("forecast")
  fun get5DayEvery3HourForecastByCityId(
      @Query("id") id: Long
  ): Single<FiveDayForecastResponse>
}