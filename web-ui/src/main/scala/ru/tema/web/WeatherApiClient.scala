package ru.tema.web

import java.time.LocalDate

import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import upickle.default.read

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Location(lat: Double, lon: Double)
case class City(title: String, location: Location)


class WeatherApiClient {

  private val endpoint = "http://localhost:8080"

  def locations(cities: Seq[String]): Future[Seq[City]] = {
    val query = cities.map(c => s"city=$c").mkString("&")
    Ajax.get(s"$endpoint/locations?$query")
      .map(validateResponse)
      .map(res => read[Seq[City]](res.responseText))
  }

  // TODO: change return type
  def history(locs: Seq[Location], date: LocalDate, days: Int): Future[Seq[String]] = {
    val futures = locs.map(l => historyForLocation(l, date, days))
    Future.sequence(futures)
  }

  private def historyForLocation(location: Location, date: LocalDate, days: Int): Future[String] = {
    val query = s"lat=${location.lat}&lon=${location.lon}&date=${formatDate(date)}&days=$days"
    Ajax.get(s"$endpoint/history?$query")
      .map(validateResponse)
      .map(res => res.responseText) // TODO: parse response
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
