package xyz.nmasnadithya.openweather.worker

import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Worker util
 * Enqueue and cancel update weather worker
 */
object WorkerUtil {
  /**
   * Executor object that runs each task in the thread that invokes
   * [Executor.execute] execute.
   */
  private object DirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
    override fun toString() = "DirectExecutor"
  }

  /**
   * @param [tag] The tag used to identify the work
   * @param [uniqueName] A unique name which for this operation
   * @return A [ListenableFuture] with information about [Operation]
   * [Operation.State.SUCCESS] state.
   */
  private inline fun <reified T : ListenableWorker> enqueuePeriodic(
      tag: String,
      uniqueName: String
  ): ListenableFuture<Operation.State.SUCCESS> {
    val request = PeriodicWorkRequestBuilder<T>(
        repeatInterval = 20,
        repeatIntervalTimeUnit = TimeUnit.MINUTES,
        flexTimeInterval = 5,
        flexTimeIntervalUnit = TimeUnit.MINUTES
    ).addTag(tag).build()

    return WorkManager
        .getInstance()
        .enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
        .result
  }

  /**
   *
   */
  private inline infix fun <T : Any> ListenableFuture<T>.then(crossinline listener: () -> Unit) {
    addListener({ listener() }, DirectExecutor)
  }

  /**
   * @param [tag] The tag used to identify the work
   * @return A [ListenableFuture] with information about [Operation]
   * [Operation.State.SUCCESS] state.
   */
  @JvmStatic
  private fun cancelAllWorkByTag(tag: String): ListenableFuture<Operation.State.SUCCESS> {
    return WorkManager.getInstance().cancelAllWorkByTag(tag).result
  }

  /**
   * Enqueue update current weather work request
   */
  @JvmStatic
  fun enqueueUpdateCurrentWeatherWorkRequest() {
    val tag = UpdateCurrentWeatherWorker.TAG

    enqueuePeriodic<UpdateCurrentWeatherWorker>(
        uniqueName = UpdateCurrentWeatherWorker.UNIQUE_WORK_NAME,
        tag = tag
    ) then { Timber.d("Enqueued Current weather worker") }
  }

  /**
   * Enqueue update daily weather work request
   */
  @JvmStatic
  fun enqueueUpdateDailyWeatherWorkRequest() {
    val tag = UpdateDailyWeatherWorker.TAG

    enqueuePeriodic<UpdateDailyWeatherWorker>(
        uniqueName = UpdateDailyWeatherWorker.UNIQUE_WORK_NAME,
        tag = tag
    ) then { Timber.d("Enqueued Daily weather worker") }
  }

  /**
   * Cancel update current weather work request
   */
  @JvmStatic
  fun cancelUpdateCurrentWeatherWorkRequest() {
    val tag = UpdateCurrentWeatherWorker.TAG
    cancelAllWorkByTag(tag) then { Timber.d("Cancel current weather worker") }
  }

  /**
   * Cancel update daily weather work request
   */
  @JvmStatic
  fun cancelUpdateDailyWeatherWorkRequest() {
    val tag = UpdateDailyWeatherWorker.TAG
    cancelAllWorkByTag(tag) then { Timber.d("Cancelled Daily weather worker") }
  }
}
