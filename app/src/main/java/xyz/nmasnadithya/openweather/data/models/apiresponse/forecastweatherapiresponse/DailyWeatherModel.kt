package xyz.nmasnadithya.openweather.data.models.apiresponse.forecastweatherapiresponse

import xyz.nmasnadithya.openweather.data.models.apiresponse.*
import com.squareup.moshi.Json

class DailyWeatherModel(
    /**
     * Time of data forecasted, unix, UTC
     */
    @Json(name = "dt")
    val dt: Long? = null,

    /**
     * Main information
     */
    @Json(name = "main")
    val main: Main? = null,

    /**
     * More info Weather condition codes
     */
    @Json(name = "weather")
    val weather: List<WeatherModel>? = null,

    /**
     * Cloud
     */
    @Json(name = "clouds")
    val clouds: Clouds? = null,

    /**
     * Wind
     */
    @Json(name = "wind")
    val wind: Wind? = null,


    /**
     * Rain
     */
    @Json(name = "rain")
    val rain: Rain? = null,

    /**
     * Snow
     */
    @Json(name = "snow")
    val snow: Snow? = null,

    @Json(name = "sys")
    val sys: Sys? = null,

    /**
     * Data/time of calculation, UTC
     */
    @Json(name = "dt_txt")
    val dtTxt: String? = null
)