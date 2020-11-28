package xyz.nmasnadithya.openweather.ui.main

import android.app.Application
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import xyz.nmasnadithya.openweather.R
import xyz.nmasnadithya.openweather.data.CurrentWeatherRepository
import xyz.nmasnadithya.openweather.ui.main.MainContract.ViewState.CityAndWeather
import xyz.nmasnadithya.openweather.ui.main.MainContract.ViewState.NoSelectedCity
import xyz.nmasnadithya.openweather.utils.None
import xyz.nmasnadithya.openweather.utils.Some
import xyz.nmasnadithya.openweather.utils.themeColor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import timber.log.Timber

class MainPresenter(
    currentWeatherRepository: CurrentWeatherRepository,
    private val colorHolderSource: ColorHolderSource,
    private val androidApplication: Application
) : MviBasePresenter<MainContract.View, MainContract.ViewState>() {
  private var disposable: Disposable? = null

  private val state = Observables.combineLatest(
      source1 = currentWeatherRepository.getSelectedCityAndCurrentWeatherOfSelectedCity(),
      source2 = colorHolderSource.colorObservable
  ).map {
    when (val optional = it.first) {
      None -> NoSelectedCity(androidApplication.themeColor(R.attr.colorPrimaryVariant))
      is Some -> CityAndWeather(
          city = optional.value.city,
          weather = optional.value.currentWeather,
          vibrantColor = it.second.first
      )
    }
  }
      .distinctUntilChanged()
      .doOnNext { Timber.d("New ViewState $it") }
      .observeOn(AndroidSchedulers.mainThread())!!

  override fun bindIntents() {
    disposable = intent(MainContract.View::changeColorIntent)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { Timber.d("Changing color $it") }
        .subscribe(colorHolderSource::change)

    subscribeViewState(state, MainContract.View::render)
  }

  override fun unbindIntents() {
    super.unbindIntents()
    disposable?.takeUnless { it.isDisposed }?.dispose()
  }

}
