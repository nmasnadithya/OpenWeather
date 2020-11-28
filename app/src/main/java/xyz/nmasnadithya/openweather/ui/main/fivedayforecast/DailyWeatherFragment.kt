package xyz.nmasnadithya.openweather.ui.main.fivedayforecast

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hannesdorfmann.mosby3.mvi.MviFragment
import xyz.nmasnadithya.openweather.R
import xyz.nmasnadithya.openweather.ui.main.MainActivity
import xyz.nmasnadithya.openweather.ui.main.fivedayforecast.DailyWeatherContract.RefreshIntent
import xyz.nmasnadithya.openweather.utils.snackBar
import xyz.nmasnadithya.openweather.utils.ui.HeaderItemDecoration
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_daily_weather.*
import timber.log.Timber

@ExperimentalStdlibApi
class DailyWeatherFragment : MviFragment<DailyWeatherContract.View, DailyWeatherPresenter>(),
    DailyWeatherContract.View {

  private var errorSnackBar: Snackbar? = null
  private var refreshSnackBar: Snackbar? = null

  private val dailyWeatherAdapter = DailyWeatherAdapter()
  private val compositeDisposable = CompositeDisposable()
  private val initialRefreshSubject = PublishSubject.create<RefreshIntent.InitialRefreshIntent>()

  override fun refreshDailyWeatherIntent(): Observable<RefreshIntent> {
    return swipe_refresh_layout.refreshes()
        .map { RefreshIntent.UserRefreshIntent }
        .cast<RefreshIntent>()
        .mergeWith(initialRefreshSubject)
        .doOnNext { Timber.d("Refreshing Daily weather") }
  }

  override fun createPresenter() = (requireActivity() as MainActivity).lifecycleScope.get<DailyWeatherPresenter>()

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View = inflater.inflate(R.layout.fragment_daily_weather, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recycler_daily_weathers.run {
      setHasFixedSize(true)
      val linearLayoutManager = LinearLayoutManager(context)
      layoutManager = linearLayoutManager
      adapter = dailyWeatherAdapter

      DividerItemDecoration(context, linearLayoutManager.orientation)
          .apply {
            ContextCompat.getDrawable(
                context,
                R.drawable.daily_weather_divider
            )?.let(::setDrawable)
          }
          .let(::addItemDecoration)
      addItemDecoration(HeaderItemDecoration(dailyWeatherAdapter))
    }
  }

  override fun onResume() {
    super.onResume()
    initialRefreshSubject.onNext(RefreshIntent.InitialRefreshIntent)
    dailyWeatherAdapter
        .clickObservable
        .subscribeBy(onNext = ::showDetail)
        .addTo(compositeDisposable)
  }

  private fun showDetail(item: DailyWeatherListItem.Weather) {
    val context = requireContext()
    context.startActivity(
        Intent(context, DailyDetailActivity::class.java).apply {
          putExtra(DailyDetailActivity.ITEM_KEY, item)
        }
    )
  }

  override fun onPause() {
    super.onPause()
    compositeDisposable.clear()
  }

  override fun render(viewState: DailyWeatherContract.ViewState) {
    swipe_refresh_layout.isRefreshing = false

    dailyWeatherAdapter.submitList(viewState.dailyWeatherListItem)

    if (viewState.error != null && viewState.showError) {
      errorSnackBar?.dismiss()
      errorSnackBar = view?.snackBar(viewState.error.message ?: "An error occurred")
    }
    if (!viewState.showError) {
      errorSnackBar?.dismiss()
    }

    if (viewState.showRefreshSuccessfully) {
      refreshSnackBar?.dismiss()
      refreshSnackBar = view?.snackBar("Refresh successfully")
    } else {
      refreshSnackBar?.dismiss()
    }
  }
}
