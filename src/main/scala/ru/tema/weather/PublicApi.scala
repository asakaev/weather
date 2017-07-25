package ru.tema.weather

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import ru.tema.darksky.Location
import ru.tema.repository.CitiesRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class City(title: String, location: Location)


class PublicApi(weatherService: WeatherService, repo: CitiesRepo) {

  def locations(cities: Seq[String]): Future[Seq[City]] = {
    def toCity(title: String) = repo.location(title).map(_.map(loc => City(title, loc)))
    val futures = cities.map(toCity)
    Future.sequence(futures).map(_.flatten)
  }

  def history(location: Location, date: String, days: Int): Future[HistoryResponse] = {
    val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE) // TODO: unsafe
    println(s"parsed local date: $localDate")
    weatherService.getHistory(location, localDate, days)
  }

}
