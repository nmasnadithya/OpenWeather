package xyz.nmasnadithya.openweather.initializer

import android.content.Context
import androidx.startup.Initializer
import com.google.android.libraries.places.api.Places
import xyz.nmasnadithya.openweather.BuildConfig
import timber.log.Timber

@Suppress("unused")
class PlacesApiInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    Timber.d("Initialized PlacesAPI")

    Places.initialize(context, BuildConfig.PLACE_API_KEY)
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}