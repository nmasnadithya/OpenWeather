package xyz.nmasnadithya.openweather.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.media.RingtoneManager.TYPE_NOTIFICATION
import androidx.core.app.NotificationCompat
import androidx.core.text.HtmlCompat
import xyz.nmasnadithya.openweather.CancelNotificationReceiver
import xyz.nmasnadithya.openweather.R
import xyz.nmasnadithya.openweather.data.local.SettingPreferences
import xyz.nmasnadithya.openweather.data.models.TemperatureUnit
import xyz.nmasnadithya.openweather.data.models.entity.City
import xyz.nmasnadithya.openweather.data.models.entity.CityAndCurrentWeather
import xyz.nmasnadithya.openweather.data.models.entity.CurrentWeather
import xyz.nmasnadithya.openweather.ui.SplashActivity
import xyz.nmasnadithya.openweather.utils.ui.getIconDrawableFromCurrentWeather
import timber.log.Timber
import xyz.nmasnadithya.openweather.BuildConfig
import java.time.format.DateTimeFormatter
import java.util.*

const val WEATHER_NOTIFICATION_ID = 2
const val ACTION_CANCEL_NOTIFICATION = "${BuildConfig.APPLICATION_ID}.CancelNotificationReceiver"

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yy")

@ExperimentalStdlibApi
fun Context.showOrUpdateNotification(
    weather: CurrentWeather,
    city: City,
    unit: TemperatureUnit,
    popUpAndSound: Boolean // TODO:something is wrong
) {
  val temperature = unit.format(weather.temperature)
  val text = HtmlCompat.fromHtml(
      """$temperature
      |<br>
      |${weather.description.capitalize(Locale.ROOT)}
      |<br>
      |<i>Update time: ${weather.dataTime.toZonedDateTime(city.zoneId).format(DATE_TIME_FORMATTER)}</i>
      """.trimMargin(),
      HtmlCompat.FROM_HTML_MODE_LEGACY
  )
  val notification = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
      .setSmallIcon(
          getIconDrawableFromCurrentWeather(
              weatherConditionId = weather.weatherConditionId,
              weatherIcon = weather.icon
          )
      )
      .setContentTitle("${city.name} - ${city.country}")
      .setContentText(temperature)
      .setStyle(NotificationCompat.BigTextStyle().bigText(text))
      .addAction(
          R.drawable.ic_close,
          "Dismiss",
          PendingIntent.getBroadcast(
              this,
              0,
              Intent(this, CancelNotificationReceiver::class.java).apply {
                action = ACTION_CANCEL_NOTIFICATION
              },
              PendingIntent.FLAG_CANCEL_CURRENT
          )
      )
      .setAutoCancel(false)
      .setOngoing(true)
      .setWhen(System.currentTimeMillis())
      .apply {
        if (popUpAndSound) {
          priority = NotificationCompat.PRIORITY_HIGH
          setDefaults(NotificationCompat.DEFAULT_ALL)
          setSound(RingtoneManager.getDefaultUri(TYPE_NOTIFICATION))
        }

        val resultPendingIntent = PendingIntent.getActivity(
            this@showOrUpdateNotification,
            0,
            Intent(applicationContext, SplashActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        setContentIntent(resultPendingIntent)
      }.build()

  Timber.d("Show or update notification = $notification, weather = $weather, city = $city, unit = $unit, popUpAndSound = $popUpAndSound")

  (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
      WEATHER_NOTIFICATION_ID,
      notification
  )
}

fun Context.cancelNotificationById(id: Int) =
    (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        .cancel(id).also { Timber.d("Cancel notification $id") }


@ExperimentalStdlibApi
fun Context.showNotificationIfEnabled(
    cityAndCurrentWeather: CityAndCurrentWeather,
    settingPreferences: SettingPreferences
) {
  Timber.d("Going to show notification. content: $cityAndCurrentWeather, preference: $settingPreferences ")

  if (settingPreferences.showNotificationPreference.value) {
    showOrUpdateNotification(
        weather = cityAndCurrentWeather.currentWeather,
        city = cityAndCurrentWeather.city,
        unit = settingPreferences.temperatureUnitPreference.value,
        popUpAndSound = settingPreferences.soundNotificationPreference.value
    )
  }
}