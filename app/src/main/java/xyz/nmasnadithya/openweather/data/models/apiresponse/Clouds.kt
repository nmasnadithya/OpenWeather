package xyz.nmasnadithya.openweather.data.models.apiresponse

import com.squareup.moshi.Json

class Clouds(
    /**
     * Cloudiness, %
     */
    @Json(name = "all")
    val all: Long? = null
)