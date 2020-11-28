package xyz.nmasnadithya.openweather.ui.main

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Target
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import xyz.nmasnadithya.openweather.R
import xyz.nmasnadithya.openweather.data.models.entity.City
import xyz.nmasnadithya.openweather.data.models.entity.CurrentWeather
import xyz.nmasnadithya.openweather.ui.BaseMviActivity
import xyz.nmasnadithya.openweather.ui.cities.CitiesActivity
import xyz.nmasnadithya.openweather.ui.main.chart.ChartFragment
import xyz.nmasnadithya.openweather.ui.main.currentweather.CurrentWeatherFragment
import xyz.nmasnadithya.openweather.ui.main.fivedayforecast.DailyWeatherFragment
import xyz.nmasnadithya.openweather.ui.setting.SettingsActivity
import xyz.nmasnadithya.openweather.utils.*
import xyz.nmasnadithya.openweather.utils.blur.GlideBlurTransformation
import xyz.nmasnadithya.openweather.utils.ui.ZoomOutPageTransformer
import xyz.nmasnadithya.openweather.utils.ui.getBackgroundDrawableFromWeather

@ExperimentalStdlibApi
class MainActivity : BaseMviActivity<MainContract.View, MainPresenter>(createScope = true),
    MainContract.View {
    private val RC_SIGN_IN: Int = 972
    private val changeBackground = PublishSubject.create<Optional<Bitmap>>()

    private var target1: CustomViewTarget<*, *>? = null
    private var target2: CustomViewTarget<*, *>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(FLAG_TRANSLUCENT_STATUS)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_playlist_add_white_24dp)
        }

        setupViewPager()
    }

    private fun setupViewPager() {
        view_pager.run {
            val fragments: List<Fragment> = listOf(
                CurrentWeatherFragment(),
                DailyWeatherFragment(),
                ChartFragment()
            )
            adapter = SectionsPagerAdapter(
                supportFragmentManager,
                fragments
            )
            offscreenPageLimit = fragments.size

            setPageTransformer(true, ZoomOutPageTransformer())

            dots_indicator.setViewPager(view_pager)
            dots_indicator.dotsClickable = true
        }
    }

    private fun enableIndicatorAndViewPager(isEnable: Boolean) {
        if (isEnable) {
            dots_indicator.visibility = View.VISIBLE
            view_pager.pagingEnable = true
        } else {
            dots_indicator.visibility = View.INVISIBLE
            view_pager.setCurrentItem(0, true)
            view_pager.pagingEnable = false
        }
    }

    private class SectionsPagerAdapter(
        fm: FragmentManager,
        private val fragments: List<Fragment>
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int) = fragments[position]
        override fun getCount() = fragments.size
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> true.also { startActivity<CitiesActivity>() }
            R.id.action_settings -> true.also { startActivity<SettingsActivity>() }
            R.id.action_account -> true.also {startAuth()}
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startAuth() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.GitHubBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    private fun updateBackground(
        weather: CurrentWeather,
        city: City
    ) {
        Glide
            .with(this)
            .apply {
                clear(target1)
                clear(target2)
                changeBackground.onNext(None)
            }
            .asBitmap()
            .load(getBackgroundDrawableFromWeather(weather, city))
            .apply(
                RequestOptions
                    .bitmapTransform(GlideBlurTransformation(this, 20f))
                    .fitCenter()
                    .centerCrop()
            )
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(object : CustomViewTarget<ImageView, Bitmap>(image_background) {
                override fun onLoadFailed(errorDrawable: Drawable?) = Unit

                override fun onResourceCleared(placeholder: Drawable?) = Unit

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    view.setImageBitmap(resource)
                    changeBackground.onNext(Some(resource))
                }
            })
            .also { target1 = it }
    }

    override fun render(state: MainContract.ViewState) {
        window.statusBarColor = state.vibrantColor
        when (state) {
            is MainContract.ViewState.NoSelectedCity -> renderNoSelectedCity()
            is MainContract.ViewState.CityAndWeather -> renderCityAndWeather(state)
        }
    }

    override fun changeColorIntent(): Observable<Pair<Int, Int>> {
        return changeBackground
            .switchMap { optional ->
                when (optional) {
                    is Some -> {
                        Observable
                            .fromCallable {
                                getVibrantColor(
                                    resource = optional.value,
                                    colorPrimaryVariant = themeColor(R.attr.colorPrimaryVariant),
                                    colorSecondary = themeColor(R.attr.colorSecondary),
                                )
                            }
                            .subscribeOn(Schedulers.computation())
                    }
                    None -> Observable.empty()
                }
            }
    }

    private fun renderCityAndWeather(state: MainContract.ViewState.CityAndWeather) {
        updateBackground(state.weather, state.city)

        toolbar_title.text = getString(
            R.string.city_name_and_country,
            state.city.name,
            state.city.country
        )
        enableIndicatorAndViewPager(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = Firebase.auth.currentUser

            }
        }
    }

    private fun renderNoSelectedCity() {
        Glide.with(this)
            .apply {
                clear(target1)
                clear(target2)
                changeBackground.onNext(None)
            }
            .load(R.drawable.default_bg)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions.fitCenterTransform().centerCrop())
            .apply(RequestOptions.bitmapTransform(GlideBlurTransformation(this, 25f)))
            .into(object : CustomViewTarget<ImageView, Drawable>(image_background) {
                override fun onLoadFailed(errorDrawable: Drawable?) = Unit
                override fun onResourceCleared(placeholder: Drawable?) = Unit
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    view.setImageDrawable(resource)
                }
            })
            .also { target2 = it }

        toolbar_title.text = getString(R.string.no_selected_city)
        enableIndicatorAndViewPager(false)
    }

    override fun createPresenter() = lifecycleScope.get<MainPresenter>()
}

@WorkerThread
private fun getVibrantColor(
    resource: Bitmap,
    @ColorInt colorPrimaryVariant: Int,
    @ColorInt colorSecondary: Int,
): Pair<Int, Int> {
    return Palette
        .from(resource)
        .generate()
        .let { palette ->
            @ColorInt val darkColor = listOf(
                palette.getSwatchForTarget(Target.DARK_VIBRANT)?.rgb,
                palette.getSwatchForTarget(Target.VIBRANT)?.rgb,
                palette.getSwatchForTarget(Target.LIGHT_VIBRANT)?.rgb,
                palette.getSwatchForTarget(Target.DARK_MUTED)?.rgb,
                palette.getSwatchForTarget(Target.MUTED)?.rgb,
                palette.getSwatchForTarget(Target.DARK_MUTED)?.rgb
            ).find { it !== null } ?: colorPrimaryVariant

            @ColorInt val lightColor = listOf(
                palette.getSwatchForTarget(Target.LIGHT_VIBRANT)?.rgb,
                palette.getSwatchForTarget(Target.LIGHT_MUTED)?.rgb
            ).find { it !== null } ?: colorSecondary

            darkColor to lightColor
        }
}
