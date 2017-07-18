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

  def handleResults(results: Seq[HistoryResponse]) = {
    val day1 = results.head.days.flatMap(_.dataPoints)
    val day2 = results.last.days.flatMap(_.dataPoints)
    jQuery("body").append(s"<p>Day1: $day1</p>")
    jQuery("body").append(s"<p>Day2: $day2</p>")
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
      handleResults(results)
      Unit
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
