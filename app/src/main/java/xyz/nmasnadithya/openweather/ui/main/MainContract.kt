package xyz.nmasnadithya.openweather.ui.main

import androidx.annotation.ColorInt
import com.hannesdorfmann.mosby3.mvp.MvpView
import xyz.nmasnadithya.openweather.data.models.entity.City
import xyz.nmasnadithya.openweather.data.models.entity.CurrentWeather
import io.reactivex.Observable

interface MainContract {
  sealed class ViewState {
    abstract val vibrantColor: Int

    data class CityAndWeather(
        val city: City,
        val weather: CurrentWeather,
        @ColorInt override val vibrantColor: Int
    ) : ViewState()

    data class NoSelectedCity(@ColorInt override val vibrantColor: Int) : ViewState()
  }

  interface View : MvpView {
    fun changeColorIntent(): Observable<Pair<Int, Int>>

    fun render(state: ViewState)
  }
}