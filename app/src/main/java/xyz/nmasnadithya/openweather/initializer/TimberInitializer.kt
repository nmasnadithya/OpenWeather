package xyz.nmasnadithya.openweather.initializer

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import xyz.nmasnadithya.openweather.BuildConfig
import xyz.nmasnadithya.openweather.utils.CrashReportingTree
import timber.log.Timber

@Suppress("unused")
class TimberInitializer : Initializer<Unit> {
  override fun create(context: Context) {

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    } else {
      Timber.plant(CrashReportingTree(Firebase.crashlytics))
    }
    Timber.d("Initialized Timber")
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}