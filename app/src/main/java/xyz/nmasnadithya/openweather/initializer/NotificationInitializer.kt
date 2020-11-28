package xyz.nmasnadithya.openweather.initializer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import androidx.startup.Initializer
import xyz.nmasnadithya.openweather.R
import timber.log.Timber

@Suppress("unused")
class NotificationInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    Timber.d("Initialized Notification")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
          context.getString(R.string.notification_channel_id),
          context.getString(R.string.notification_channel_name),
          NotificationManager.IMPORTANCE_DEFAULT
      ).apply { description = "Notification channel of weather app" }

      getSystemService(context, NotificationManager::class.java)!!.createNotificationChannel(channel)
    }
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}