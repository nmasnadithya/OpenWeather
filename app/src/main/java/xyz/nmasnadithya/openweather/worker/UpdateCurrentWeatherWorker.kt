package xyz.nmasnadithya.openweather.worker

import android.app.Application
import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import xyz.nmasnadithya.openweather.data.CurrentWeatherRepository
import xyz.nmasnadithya.openweather.data.NoSelectedCityException
import xyz.nmasnadithya.openweather.data.local.SettingPreferences
import xyz.nmasnadithya.openweather.initializer.startKoinIfNeeded
import xyz.nmasnadithya.openweather.utils.WEATHER_NOTIFICATION_ID
import xyz.nmasnadithya.openweather.utils.cancelNotificationById
import xyz.nmasnadithya.openweather.utils.showNotificationIfEnabled
import xyz.nmasnadithya.openweather.worker.WorkerUtil.cancelUpdateCurrentWeatherWorkRequest
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import xyz.nmasnadithya.openweather.BuildConfig

class UpdateCurrentWeatherWorker(
    context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams), KoinComponent {

  private val currentWeatherRepository by inject<CurrentWeatherRepository>()
  private val settingPreferences by inject<SettingPreferences>()

  init {
    (applicationContext as Application).startKoinIfNeeded()
  }

  @ExperimentalStdlibApi
  override fun createWork(): Single<Result> {
    return currentWeatherRepository
        .refreshCurrentWeatherOfSelectedCity()
        .doOnSubscribe { Timber.d("Started worker") }
        .doOnSuccess {
          Timber.d("Worker completed: $it")
          applicationContext.showNotificationIfEnabled(it, settingPreferences)
        }
        .doOnError {
          Timber.d(it, "Worker failed")

          if (it is NoSelectedCityException) {
            Timber.d("Canceling worker & notification, no city")
            applicationContext.cancelNotificationById(WEATHER_NOTIFICATION_ID)
            cancelUpdateCurrentWeatherWorkRequest()
          }
        }
        .map { Result.success(workDataOf(RESULT to "Update current success")) }
        .onErrorReturn { Result.failure(workDataOf(RESULT to "Update current failure: ${it.message}")) }
  }

  companion object {
    const val UNIQUE_WORK_NAME = "${BuildConfig.APPLICATION_ID}.worker.UpdateCurrentWeatherWorker"
    const val TAG = "${BuildConfig.APPLICATION_ID}.worker.UpdateCurrentWeatherWorker"
    private const val RESULT = "RESULT"
  }
}