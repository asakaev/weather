package ru.tema.web

import java.time.LocalDate

import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// TODO: domain model leak
//case class DataPoint(time: Long, temperature: Double, humidity: Double, windSpeed: Double, windBearing: Double)
case class DataPoint(time: Long, temperature: Double) // Long type problem
// hack

// TODO: share
case class Location(lat: Double, lon: Double)
case class City(title: String, location: Location)

// TODO: share2

//case class Stats(standardDeviation: Double, median: Double, min: Double, max: Double)
//case class DetailedStats(temp: Stats, humidity: Stats, windStrength: Stats, windBearing: Stats)
//case class DayStats(twentyFourHours: DetailedStats, day: DetailedStats, night: DetailedStats)
case class Day(dataPoints: Seq[DataPoint]) // POW: zonedDateTime: ZonedDateTime,  dayStats: DayStats

//case class DayNightHours(dayHours: Seq[DataPoint], nightHours: Seq[DataPoint])
case class HistoryResponse(days: Seq[Day]) // POW! -> overallStats: DetailedStats


class WeatherApiClient(endpoint: String) {

  def locations(cities: Seq[String]): Future[Seq[City]] = {
    def parseJson(s: String) = decode[Seq[City]](s).right.get

    val query = cities.map(c => s"city=$c").mkString("&")
    Ajax.get(s"$endpoint/locations?$query")
      .map(validateResponse)
      .map(_.responseText)
      .map(parseJson)
  }

  def history(locations: Seq[Location], date: LocalDate, days: Int): Future[Seq[HistoryResponse]] = {
    val futures = locations.map(l => historyForLocation(l, date, days))
    Future.sequence(futures)
  }

  private def historyForLocation(location: Location, date: LocalDate, days: Int): Future[HistoryResponse] = {
     def parseJson(s: String) = decode[HistoryResponse](s).right.get

    val query = s"lat=${location.lat}&lon=${location.lon}&date=${formatDate(date)}&days=$days"
    Ajax.get(s"$endpoint/history?$query")
      .map(validateResponse)
      .map(res => {
        println(s"RESULT: ${res.responseText}")
        res
      })
      .map(_.responseText)
      .map(parseJson)
  }

  private def validateResponse(response: XMLHttpRequest): XMLHttpRequest = {
    println(s"Status: ${response.status}, Content: ${response.responseText}")
    response.status match {
      case 200 => response
      case _ => throw new Exception(s"Server respond with status code ${response.status}")
    }
  }

  // DateTimeFormatter doesn't work in Scala.js: "dd-MM-yyyy"
  private def formatDate(date: LocalDate): String = {
    val split = date.toString.split("-")
    s"${split(2)}-${split(1)}-${split(0)}"
  }
}
