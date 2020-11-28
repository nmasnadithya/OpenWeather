package xyz.nmasnadithya.openweather.data.local

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import xyz.nmasnadithya.openweather.data.models.entity.City
import timber.log.Timber

@Dao
abstract class CityDao {
  @Insert(onConflict = IGNORE)
  abstract fun insertCity(city: City): Long

  @Update
  abstract fun updateCity(city: City)

  @Delete
  abstract fun deleteCity(city: City)

  @Transaction
  open fun upsert(city: City) {
    insertCity(city)
        .takeIf {
          Timber.d("Inserting City: $it")
          it == -1L
        }
        ?.let { updateCity(city) }
  }
}