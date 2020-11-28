package xyz.nmasnadithya.openweather.ui.main

import android.app.Application
import androidx.annotation.MainThread
import xyz.nmasnadithya.openweather.R
import xyz.nmasnadithya.openweather.utils.asObservable
import xyz.nmasnadithya.openweather.utils.themeColor
import io.reactivex.subjects.BehaviorSubject

class ColorHolderSource(androidApplication: Application) {
  private val subject = BehaviorSubject.createDefault(
      androidApplication.themeColor(R.attr.colorPrimaryVariant) to
          androidApplication.themeColor(R.attr.colorSecondary)
  )

  val colorObservable = subject.asObservable()

  @MainThread
  fun change(colors: Pair<Int, Int>) = subject.onNext(colors)
}