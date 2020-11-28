package xyz.nmasnadithya.openweather.data.local

import androidx.room.*
import xyz.nmasnadithya.openweather.data.models.entity.DailyWeather
import io.reactivex.Observable

@Dao
abstract class FiveDayForecastDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertDailyWeathers(weathers: List<DailyWeather>)

  @Query("DELETE FROM five_day_forecast WHERE city_id = :cityId")
  abstract fun deleteAllDailyWeathersByCityId(cityId: Long)

  @Query("SELECT * FROM five_day_forecast WHERE city_id = :cityId ORDER BY data_time")
  abstract fun getAllDailyWeathersByCityId(cityId: Long): Observable<List<DailyWeather>>

  @Transaction
  open fun deleteDailyWeathersByCityIdAndInsert(cityId: Long, weathers: List<DailyWeather>) {
    deleteAllDailyWeathersByCityId(cityId)
    insertDailyWeathers(weathers)
  }
}