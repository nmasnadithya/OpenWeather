package xyz.nmasnadithya.openweather.ui.main.currentweather

import android.app.Application
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import xyz.nmasnadithya.openweather.data.CityRepository
import xyz.nmasnadithya.openweather.data.CurrentWeatherRepository
import xyz.nmasnadithya.openweather.data.NoSelectedCityException
import xyz.nmasnadithya.openweather.data.local.SettingPreferences
import xyz.nmasnadithya.openweather.data.models.PressureUnit
import xyz.nmasnadithya.openweather.data.models.SpeedUnit
import xyz.nmasnadithya.openweather.data.models.TemperatureUnit
import xyz.nmasnadithya.openweather.data.models.WindDirection
import xyz.nmasnadithya.openweather.data.models.entity.CityAndCurrentWeather
import xyz.nmasnadithya.openweather.ui.main.currentweather.CurrentWeatherContract.*
import xyz.nmasnadithya.openweather.utils.*
import xyz.nmasnadithya.openweather.utils.Optional
import xyz.nmasnadithya.openweather.worker.WorkerUtil
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

@ExperimentalStdlibApi
class CurrentWeatherPresenter(
    private val currentWeatherRepository: CurrentWeatherRepository,
    private val cityRepository: CityRepository,
    private val androidApplication: Application,
    private val settingPreferences: SettingPreferences
) : MviBasePresenter<View, ViewState>() {

  private val cityAndWeatherPartialChange = Observables.combineLatest(
      source1 = settingPreferences.speedUnitPreference.observable,
      source2 = settingPreferences.pressureUnitPreference.observable,
      source3 = settingPreferences.temperatureUnitPreference.observable,
      source4 = currentWeatherRepository.getSelectedCityAndCurrentWeatherOfSelectedCity(),
      combineFunction = { speedUnit, pressureUnit, temperatureUnit, optional ->
        Tuple4(
            speedUnit,
            pressureUnit,
            temperatureUnit,
            optional
        )
      }
  ).switchMap { (speedUnit, pressureUnit, temperatureUnit, optional) ->
    when (optional) {
      None -> showError(NoSelectedCityException)
      is Some -> Observable.just(
          toCurrentWeather(
              optional.value,
              speedUnit,
              pressureUnit,
              temperatureUnit
          )
      ).map<PartialStateChange> { PartialStateChange.Weather(it) }
    }.onErrorResumeNext(::showError)
  }

  private val refreshWeatherProcessor =
      ObservableTransformer<RefreshIntent, PartialStateChange> { intentObservable ->
        intentObservable
            .publish { shared ->
              Observable.mergeArray(
                  shared.ofType<RefreshIntent.InitialRefreshIntent>()
                      .take(1)
                      .delay { cityRepository.getSelectedCity().filter { it is Some } },
                  shared.notOfType<RefreshIntent.InitialRefreshIntent>()
              )
            }
            .exhaustMap {
              currentWeatherRepository
                  .refreshCurrentWeatherOfSelectedCity()
                  .doOnSuccess {
                    if (settingPreferences.autoUpdatePreference.value) {
                      WorkerUtil.enqueueUpdateCurrentWeatherWorkRequest()
                    }
                    androidApplication.showNotificationIfEnabled(it, settingPreferences)
                  }
                  .doOnError {
                    if (it is NoSelectedCityException) {
                      androidApplication.cancelNotificationById(WEATHER_NOTIFICATION_ID)
                      WorkerUtil.cancelUpdateCurrentWeatherWorkRequest()
                      WorkerUtil.cancelUpdateDailyWeatherWorkRequest()
                    }
                  }
                  .toObservable()
                  .observeOn(AndroidSchedulers.mainThread())
                  .switchMap {
                    Observable
                        .timer(2_000, TimeUnit.MILLISECONDS)
                        .map<PartialStateChange> { PartialStateChange.RefreshWeatherSuccess(showMessage = false) }
                        .startWith(PartialStateChange.RefreshWeatherSuccess(showMessage = true))
                  }
                  .onErrorResumeNext(::showError)
            }
      }

  override fun bindIntents() {
    subscribeViewState(
        Observable.mergeArray(
            intent(View::refreshCurrentWeatherIntent).compose(refreshWeatherProcessor),
            cityAndWeatherPartialChange
        ).scan(ViewState(), reducer)
            .distinctUntilChanged()
            .doOnNext { Timber.d("New ViewState $it") }
            .observeOn(AndroidSchedulers.mainThread()),
        View::render
    )
  }

  private companion object {

    private val LAST_UPDATED_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")

    private data class Tuple4(
        val speedUnit: SpeedUnit,
        val pressureUnit: PressureUnit,
        val temperatureUnit: TemperatureUnit,
        val optional: Optional<CityAndCurrentWeather>
    )

    @JvmStatic
    private val reducer =
        BiFunction<ViewState, PartialStateChange, ViewState> { viewState, partialStateChange ->
          when (partialStateChange) {
            is PartialStateChange.Error -> viewState.copy(
                showError = partialStateChange.showMessage,
                error = partialStateChange.throwable,
                weather = if (partialStateChange.throwable is NoSelectedCityException) {
                  null
                } else {
                  viewState.weather
                }
            )
            is PartialStateChange.Weather -> viewState.copy(
                weather = partialStateChange.weather,
                error = null
            )
            is PartialStateChange.RefreshWeatherSuccess -> viewState.copy(
                showRefreshSuccessfully = partialStateChange.showMessage,
                error = null
            )
          }
        }

    @JvmStatic
    private fun toCurrentWeather(
        cityAndCurrentWeather: CityAndCurrentWeather,
        speedUnit: SpeedUnit,
        pressureUnit: PressureUnit,
        temperatureUnit: TemperatureUnit
    ): CurrentWeather {
      val weather = cityAndCurrentWeather.currentWeather
      val dataTimeString = weather
          .dataTime
          .toZonedDateTime(cityAndCurrentWeather.city.zoneId)
          .format(LAST_UPDATED_FORMATTER)
      return CurrentWeather(
          temperatureString = temperatureUnit.format(weather.temperature),
          pressureString = pressureUnit.format(weather.pressure),
          rainVolumeForThe3HoursMm = weather.rainVolumeForThe3Hours,
          visibilityKm = weather.visibility / 1_000,
          humidity = weather.humidity,
          description = weather.description.capitalize(Locale.ROOT),
          dataTimeString = dataTimeString,
          weatherConditionId = weather.weatherConditionId,
          weatherIcon = weather.icon,
          winSpeed = weather.winSpeed,
          winSpeedString = speedUnit.format(weather.winSpeed),
          winDirection = WindDirection.fromDegrees(weather.winDegrees).toString(),
          zoneId = cityAndCurrentWeather.city.zoneId
      )
    }

    @JvmStatic
    private fun showError(throwable: Throwable): Observable<PartialStateChange> {
      return Observable.timer(2_000, TimeUnit.MILLISECONDS)
          .map<PartialStateChange> {
            PartialStateChange.Error(throwable = throwable, showMessage = false)
          }
          .startWith(
              PartialStateChange.Error(throwable = throwable, showMessage = true)
          )
    }
  }
}