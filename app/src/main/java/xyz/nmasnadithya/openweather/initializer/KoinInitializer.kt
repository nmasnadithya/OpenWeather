package xyz.nmasnadithya.openweather.initializer

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import xyz.nmasnadithya.openweather.BuildConfig
import xyz.nmasnadithya.openweather.koin.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

@Suppress("unused")
class KoinInitializer : Initializer<Koin> {
  @ExperimentalStdlibApi
  override fun create(context: Context): Koin {
    Timber.d("Initializing Koin")
    return (context.applicationContext as Application).startKoinIfNeeded()
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

@OptIn(ExperimentalStdlibApi::class)
fun Application.startKoinIfNeeded(): Koin {
  return GlobalContext.getOrNull() ?: startKoin {
    androidLogger(
        if (BuildConfig.DEBUG) Level.DEBUG
        else Level.NONE
    )

    androidContext(this@startKoinIfNeeded)

    modules(
        listOf(
            retrofitModule,
            dataSourceModule,
            sharePrefUtilModule,
            presenterModule,
            firebaseModule
        )
    )
  }.koin
}