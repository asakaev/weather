package ru.tema.web

import org.scalajs.dom.ext.Ajax
import org.scalajs.jquery.jQuery
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global

case class Location(lat: Double, lon: Double)
case class City(title: String, location: Location)


object WeatherWebApp {

  def setupUI(): Unit = {
//    jQuery("body").append("<p>Weather WebApp</p>")
    jQuery("#locations-button").click(() => onLocations())
  }

  def onLocations(): Unit = {
    val response = Ajax.get(
      url = "http://localhost:8080/locations?city=Saint-Petersburg&city=Moscow"
    )

    // TODO: check status 200 else Throw
    response.map(res => {
      res.status match {
        case 200 =>
          println(s"response status: ${res.status}")
          val contentString = res.responseText
          println(contentString)
          val cities = read[Seq[City]](contentString)
          println(cities)
          jQuery("body").append(s"<p>$cities</p>")
        case _ =>
          println("boo1")
      }
    })
  }


  def main(args: Array[String]): Unit = {
    println("WeatherWebApp logs")
    jQuery(() => setupUI())
  }
}
