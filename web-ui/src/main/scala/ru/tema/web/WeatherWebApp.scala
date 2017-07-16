package ru.tema.web

import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global


object WeatherWebApp {

  val weatherApiClient = new WeatherApiClient

  def setupUI(): Unit = {
    jQuery("#locations-button").click(() => onLocationsBtn())
  }

  def onLocationsBtn(): Unit = {
    val locations = Seq("Saint-Petersburg", "Moscow") // TODO: get list from anywhere

    val result = for {
      cities <- weatherApiClient.locations(locations)
    } yield {
      jQuery("body").append(s"<p>$cities</p>")
    }

    result.recover {
      case e: Throwable =>
        e.printStackTrace()
        jQuery("body").append(s"<p>ERROR: ${e.getMessage}</p>")
    }
  }


  def main(args: Array[String]): Unit = {
    println("WeatherWebApp is up and running")
    setupUI()
    jQuery(() => setupUI())
  }
}
