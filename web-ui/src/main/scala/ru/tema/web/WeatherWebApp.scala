package ru.tema.web

import java.time.LocalDate

import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global


object WeatherWebApp {

  val weatherApiClient = new WeatherApiClient

  // TODO: hardcode
  private val locations = Seq("Saint-Petersburg", "Moscow")
  private val localDate = LocalDate.of(2017, 6, 16)


  def setupUI(): Unit = {
    jQuery("#locations-button").click(() => onLocationsBtn())
  }

  def onLocationsBtn(): Unit = {
    jQuery("body").append(s"<p>Locations for $locations</p>")
    println(s"date: $localDate")

    val result = for {
      cities <- weatherApiClient.locations(locations)
      results <- weatherApiClient.history(cities.map(_.location), localDate, 1)
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
