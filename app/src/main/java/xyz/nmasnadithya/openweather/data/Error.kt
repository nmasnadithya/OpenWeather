package xyz.nmasnadithya.openweather.data

object NoSelectedCityException : Exception() {
  override val message = "No selected city"
}

class SaveSelectedCityError(cause: Throwable) : Exception(cause)
