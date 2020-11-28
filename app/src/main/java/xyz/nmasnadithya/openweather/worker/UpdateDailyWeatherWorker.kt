package xyz.nmasnadithya.openweather.worker

import android.app.Application
import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import xyz.nmasnadithya.openweather.data.FiveDayForecastRepository
import xyz.nmasnadithya.openweather.data.NoSelectedCityException
import xyz.nmasnadithya.openweather.initializer.startKoinIfNeeded
import xyz.nmasnadithya.openweather.utils.WEATHER_NOTIFICATION_ID
import xyz.nmasnadithya.openweather.utils.cancelNotificationById
import xyz.nmasnadithya.openweather.worker.WorkerUtil.cancelUpdateDailyWeatherWorkRequest
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import xyz.nmasnadithya.openweather.BuildConfig

class UpdateDailyWeatherWorker(
    context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams), KoinComponent {
  private val fiveDayForecastRepository by inject<FiveDayForecastRepository>()

  init {
    (applicationContext as Application).startKoinIfNeeded()
  }

  override fun createWork(): Single<Result> {
    return fiveDayForecastRepository
        .refreshFiveDayForecastOfSelectedCity()
        .doOnSubscribe { Timber.d("Started worker") }
        .doOnSuccess { Timber.d("Worker completed: $it") }
        .doOnError {
          Timber.d(it, "Worker failed")

          if (it is NoSelectedCityException) {
            Timber.d("Canceling worker and notification")
            applicationContext.cancelNotificationById(WEATHER_NOTIFICATION_ID)
            cancelUpdateDailyWeatherWorkRequest()
          }
        }
        .map { Result.success(workDataOf(RESULT to "Update daily success")) }
        .onErrorReturn { Result.failure(workDataOf(RESULT to "Update daily failure: ${it.message}")) }
  }

  companion object {
    const val UNIQUE_WORK_NAME = "${BuildConfig.APPLICATION_ID}.worker.UpdateDailyWeatherWorker"
    const val TAG = "${BuildConfig.APPLICATION_ID}.worker.UpdateDailyWeatherWorker"
    const val RESULT = "RESULT"
  }
}