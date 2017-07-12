package ru.tema.api

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import ru.tema.darksky.Location
import ru.tema.weather.{ HistoryResponse, WeatherService }

import scala.concurrent.Future

class RestApi(weatherService: WeatherService) {

  private val inputPattern = "dd-MM-yyyy"
  private val inputFormatter = DateTimeFormatter.ofPattern(inputPattern)

  def history(lat: Double, lon: Double, date: String, days: Int): Future[HistoryResponse] = {
    val localDate = parseTime(date)
    println(s"parsed local date: $localDate")
    weatherService.getHistory(Location(lat, lon), localDate, days)
  }

  private def parseTime(input: String) = LocalDate.parse(input, inputFormatter)

}
