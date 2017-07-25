package ru.tema.web

import com.zoepepper.facades.jsjoda.format.DateTimeFormatter
import com.zoepepper.facades.jsjoda.{ LocalDate, ZonedDateTime }
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// TODO: darksky domain model leak
case class DataPoint(time: Long, temperature: Double, humidity: Double, windSpeed: Double, windBearing: Double)

// TODO: share w backend
case class Location(lat: Double, lon: Double)
case class City(title: String, location: Location)

// TODO: share w backend
case class Stats(standardDeviation: Double, median: Double, min: Double, max: Double)
case class DetailedStats(temp: Stats, humidity: Stats, windStrength: Stats, windBearing: Stats)
case class DayStats(twentyFourHours: DetailedStats, day: DetailedStats, night: DetailedStats)
case class Day(zonedDateTime: ZonedDateTime, dataPoints: Seq[DataPoint], dayStats: DayStats)

case class DayNightHours(dayHours: Seq[DataPoint], nightHours: Seq[DataPoint])
case class HistoryResponse(days: Seq[Day], overallStats: DetailedStats)

// model
case class CityHistory(city: City, historyResponse: HistoryResponse)


class WeatherApiClient(endpoint: String) {

  implicit val decodeZdt: Decoder[ZonedDateTime] = Decoder.decodeString.emap { str =>
    val zdt = ZonedDateTime.parse(str, DateTimeFormatter.ISO_OFFSET_DATE_TIME) // TODO: unsafe
    Right(zdt)
  }


  def locations(cities: Seq[String]): Future[Seq[City]] = {
    def parseJson(s: String) = decode[Seq[City]](s).right.get

    val query = cities.map(c => s"city=$c").mkString("&")
    Ajax.get(s"$endpoint/locations?$query")
      .map(validateResponse)
      .map(_.responseText)
      .map(parseJson)
  }

  def history(cities: Seq[City], date: LocalDate, days: Int): Future[Seq[CityHistory]] = {
    val futures = cities.map(city => {
      historyForLocation(city.location, date, days).map(CityHistory(city, _))
    })
    Future.sequence(futures)
  }

  private def historyForLocation(location: Location, date: LocalDate, days: Int): Future[HistoryResponse] = {
    def parseJson(s: String) = decode[HistoryResponse](s).right.get

    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val query = s"lat=${location.lat}&lon=${location.lon}&date=$dateStr&days=$days"
    Ajax.get(s"$endpoint/history?$query")
      .map(validateResponse)
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

}
