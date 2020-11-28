package xyz.nmasnadithya.openweather.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import xyz.nmasnadithya.openweather.R
import xyz.nmasnadithya.openweather.data.local.SettingPreferences
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.scope.Scope
import org.koin.ext.getFullName
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

@ExperimentalStdlibApi
abstract class BaseMviActivity<V : MvpView, P : MviPresenter<V, *>>(
    private val noActionBar: Boolean = true,
    private val createScope: Boolean = false,
) : MviActivity<V, P>() {

  val lifecycleScope: Scope by lazy(NONE) {
    check(createScope) { "createScope must be true when accessing lifecycleScope" }

    getKoin().createScope(
        this::class.getFullName() + "@" + System.identityHashCode(this),
        TypeQualifier(this::class),
        this
    )
  }

  private val settings by inject<SettingPreferences>()
  private val compositeDisposable = CompositeDisposable()

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    if (createScope) {
      getKoin()._logger.debug("Open activity scope: $lifecycleScope")
    }

    setTheme(settings.darkThemePreference.value, noActionBar)
    super.onCreate(savedInstanceState)
    observeTheme(settings.darkThemePreference.observable).addTo(compositeDisposable)
  }

  @CallSuper
  override fun onDestroy() {
    super.onDestroy()
    compositeDisposable.clear()

    if (createScope) {
      getKoin()._logger.debug("Close activity scope: $lifecycleScope")
      lifecycleScope.close()
    }
  }
}

@ExperimentalStdlibApi
abstract class BaseAppCompatActivity(private val noActionBar: Boolean = true) : AppCompatActivity() {
  private val settings by inject<SettingPreferences>()
  private val compositeDisposable = CompositeDisposable()

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(settings.darkThemePreference.value, noActionBar)
    super.onCreate(savedInstanceState)
    observeTheme(settings.darkThemePreference.observable).addTo(compositeDisposable)
  }

  @CallSuper
  override fun onDestroy() {
    super.onDestroy()
    compositeDisposable.clear()
  }
}

@ExperimentalStdlibApi
@CheckResult
private fun AppCompatActivity.observeTheme(darkThemeObservable: Observable<Boolean>): Disposable {
  return darkThemeObservable
      .distinctUntilChanged()
      .skip(1)
      .take(1)
      .subscribeBy {
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        Timber.d("Changed theme $this $it")
      }
}

private fun AppCompatActivity.setTheme(isDarkTheme: Boolean, noActionBar: Boolean) {
  setTheme(
      when {
        isDarkTheme -> {
          if (noActionBar) R.style.AppTheme_DarkTheme_NoActionBar
          else R.style.AppTheme_DarkTheme
        }
        else -> {
          if (noActionBar) R.style.AppTheme_LightTheme_NoActionBar
          else R.style.AppTheme_LightTheme
        }
      }
  )
}