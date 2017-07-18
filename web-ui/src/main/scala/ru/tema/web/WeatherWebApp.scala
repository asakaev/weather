package ru.tema.web

import java.time.LocalDate

import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global


object WeatherWebApp {

  private val endpoint = "http://localhost:8080"
  private val weatherApiClient = new WeatherApiClient(endpoint)

  // TODO: hardcode
  private val cities = Seq("Saint-Petersburg", "Moscow")
  private val localDate = LocalDate.of(2017, 6, 16)


  def setupUI(): Unit = {
    jQuery("body").append(s"<p>Cities: ${cities.mkString(", ")}</p>")
    jQuery("body").append(s"<p>Date: $localDate</p>")
    jQuery("body").append("""<button id="locations-btn" type="button">Locations</button>""")
    jQuery("#locations-btn").click(() => onLocationsBtn())
  }

  def onLocationsBtn(): Unit = {
    val result = for {
      citiesWithLocation <- weatherApiClient.locations(cities)
      results <- {
        jQuery("body").append(s"<p>$citiesWithLocation</p>")
        val locations = citiesWithLocation.map(_.location)
        weatherApiClient.history(locations, localDate, 1)
      }
    } yield {
      jQuery("body").append(s"<p>$results</p>")
    }

    result.recover {
      case e: Throwable =>
        e.printStackTrace()
        jQuery("body").append(s"<p>ERROR: ${e.getMessage}</p>")
    }
  }


  def main(args: Array[String]): Unit = {
    println("WeatherWebApp is up and running")
    jQuery(() => setupUI())
  }
}
