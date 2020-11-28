package xyz.nmasnadithya.openweather.initializer

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import xyz.nmasnadithya.openweather.worker.UpdateCurrentWeatherWorker
import xyz.nmasnadithya.openweather.worker.UpdateDailyWeatherWorker
import timber.log.Timber

@Suppress("unused")
class WorkManagerInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    Timber.d("Initializing WorkManager")

    WorkManager.initialize(
        context,
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
    )

    WorkManager.getInstance().run {
      getWorkInfosForUniqueWorkLiveData(UpdateDailyWeatherWorker.UNIQUE_WORK_NAME)
          .observeForever {
            it.forEach { workInfo ->
              Timber.d("Work: data=${workInfo.outputData.keyValueMap}, info=$workInfo")
            }
          }

      getWorkInfosForUniqueWorkLiveData(UpdateCurrentWeatherWorker.UNIQUE_WORK_NAME)
          .observeForever {
            it.forEach { workInfo ->
              Timber.d("Work: data=${workInfo.outputData.keyValueMap}, info=$workInfo")
            }
          }
    }
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}