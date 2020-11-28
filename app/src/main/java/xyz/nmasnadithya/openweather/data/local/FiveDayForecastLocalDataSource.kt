package xyz.nmasnadithya.openweather.data.local

import xyz.nmasnadithya.openweather.data.models.entity.DailyWeather
import io.reactivex.Completable
import io.reactivex.Observable

class FiveDayForecastLocalDataSource(private val fiveDayForecastDao: FiveDayForecastDao) {
  fun getAllDailyWeathersByCityId(id: Long): Observable<List<DailyWeather>> {
    return fiveDayForecastDao.getAllDailyWeathersByCityId(id)
  }

  fun deleteDailyWeathersByCityIdAndInsert(
      cityId: Long,
      weathers: List<DailyWeather>
  ): Completable {
    return Completable.fromAction {
      fiveDayForecastDao.deleteDailyWeathersByCityIdAndInsert(cityId, weathers)
    }
  }
}