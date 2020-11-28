package xyz.nmasnadithya.openweather.initializer

import android.content.Context
import androidx.startup.Initializer
import xyz.nmasnadithya.openweather.BuildConfig
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber

@Suppress("unused")
class RxJavaPluginsInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    Timber.d("Initializing RxJava")

    if (!BuildConfig.DEBUG) {
      RxJavaPlugins.setErrorHandler { Timber.d(it, "RxJava error") }
    }
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}