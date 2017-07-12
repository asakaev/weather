package ru.tema.weather

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import ru.tema.darksky.Location
import ru.tema.repository.CitiesRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PublicApi(weatherService: WeatherService, citiesRepo: CitiesRepo) {
  private val inputPattern = "dd-MM-yyyy"
  private val inputFormatter = DateTimeFormatter.ofPattern(inputPattern)

  def locations(cities: Seq[String]): Future[Seq[Location]] = {
    val futures = cities.map(citiesRepo.location)
    Future.sequence(futures).map(_.flatten)
  }

  def history(lat: Double, lon: Double, date: String, days: Int): Future[HistoryResponse] = {
    val localDate = parseTime(date)
    println(s"parsed local date: $localDate")
    weatherService.getHistory(Location(lat, lon), localDate, days)
  }

  private def parseTime(input: String) = LocalDate.parse(input, inputFormatter)

}
