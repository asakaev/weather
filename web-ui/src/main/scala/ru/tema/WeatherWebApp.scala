package ru.tema

import org.scalajs.dom.ext.Ajax
import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global


object WeatherWebApp {

  def setupUI(): Unit = {
//    jQuery("body").append("<p>Weather WebApp</p>")
    jQuery("#locations-button").click(() => onLocations())
  }

  def onLocations(): Unit = {
    val response = Ajax.get(
      url = "http://localhost:8080/locations?city=Saint-Petersburg&city=Moscow",
      headers = Map("Access-Control-Allow-Origin" -> "*")
    )

    response.map(res => {
      println(s"response status: ${res.status}")
      val txt = res.responseText
      println(res.responseText)
      jQuery("body").append(s"<p>$txt</p>")
    })
  }

  def main(args: Array[String]): Unit = {
    println("WeatherWebApp logs")
    jQuery(() => setupUI())
  }
}
