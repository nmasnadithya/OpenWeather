package xyz.nmasnadithya.openweather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import xyz.nmasnadithya.openweather.data.local.SettingPreferences
import xyz.nmasnadithya.openweather.utils.ACTION_CANCEL_NOTIFICATION
import xyz.nmasnadithya.openweather.utils.WEATHER_NOTIFICATION_ID
import xyz.nmasnadithya.openweather.utils.cancelNotificationById
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class CancelNotificationReceiver : BroadcastReceiver(), KoinComponent {
  private val settingPreferences by inject<SettingPreferences>()

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == ACTION_CANCEL_NOTIFICATION) {
      val pendingResult = goAsync()
      context.cancelNotificationById(WEATHER_NOTIFICATION_ID)

      Completable
          .fromCallable { settingPreferences.showNotificationPreference.saveActual(false) }
          .subscribeOn(Schedulers.single())
          .observeOn(AndroidSchedulers.mainThread())
          .doOnComplete { settingPreferences.showNotificationPreference.save(false) }
          .doOnTerminate { pendingResult.finish() }
          .subscribe(object : CompletableObserver {
            override fun onComplete() {
              LocalBroadcastManager
                  .getInstance(context)
                  .sendBroadcast(Intent(ACTION_CANCEL_NOTIFICATION))
              Timber.d("showNotificationPreference: Success")
            }

            override fun onSubscribe(d: Disposable) = Unit
            override fun onError(e: Throwable) = Unit
          })
    }
  }
}
