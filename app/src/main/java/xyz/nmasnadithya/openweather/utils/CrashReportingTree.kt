package xyz.nmasnadithya.openweather.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashReportingTree(private val crashlytics: FirebaseCrashlytics) : Timber.Tree() {

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    crashlytics.log(String.format("%s: %s", tag, message))
    t?.let { crashlytics.recordException(it) }
  }
}