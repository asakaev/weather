package ru.tema.web

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

  private def validateResponse(response: XMLHttpRequest): XMLHttpRequest = {
    println(s"Status: ${response.status}, Content: ${response.responseText}")
    response.status match {
      case 200 => response
      case _ => throw new Exception(s"Server respond with status code ${response.status}")
    }
  }
}
