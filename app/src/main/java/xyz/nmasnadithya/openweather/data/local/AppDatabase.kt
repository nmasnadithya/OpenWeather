package xyz.nmasnadithya.openweather.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import xyz.nmasnadithya.openweather.data.local.FiveDayForecastDao
import xyz.nmasnadithya.openweather.data.models.entity.City
import xyz.nmasnadithya.openweather.data.models.entity.CurrentWeather
import xyz.nmasnadithya.openweather.data.models.entity.DailyWeather
import java.util.*

object Converters {
  @JvmStatic
  @TypeConverter
  fun fromTimestamp(value: Long?): Date? = value?.let(::Date)

  @JvmStatic
  @TypeConverter
  fun dateToTimestamp(date: Date?): Long? = date?.time
}

@Database(
    entities = [
      CurrentWeather::class,
      City::class,
      DailyWeather::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(value = [Converters::class])
abstract class AppDatabase : RoomDatabase() {
  abstract fun cityDao(): CityDao
  abstract fun weatherDao(): CurrentWeatherDao
  abstract fun fiveDayForecastDao(): FiveDayForecastDao

  companion object {
    private const val DATABASE_NAME = "WEATHER_APP_DB"

    private val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE cities ADD COLUMN zone_id TEXT NOT NULL")
      }
    }

    @Volatile
    private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return instance ?: synchronized(this::class.java) {
        instance ?: Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()
            .also { instance = it }
      }
    }
  }
}