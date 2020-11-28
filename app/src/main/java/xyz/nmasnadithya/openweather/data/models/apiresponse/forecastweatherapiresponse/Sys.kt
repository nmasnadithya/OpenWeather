package xyz.nmasnadithya.openweather.data.models.apiresponse.forecastweatherapiresponse

import com.squareup.moshi.Json

class Sys(
    @Json(name = "pod")
    val pod: String? = null
)