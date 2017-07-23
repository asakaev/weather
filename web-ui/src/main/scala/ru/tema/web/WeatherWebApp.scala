package ru.tema.web

import java.time.LocalDate

import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }


object WeatherWebApp {

  private val endpoint = "http://localhost:8080"
  private val weatherApiClient = new WeatherApiClient(endpoint)

  // TODO: hardcode
  private val cities = Seq("Saint-Petersburg", "Moscow")
  private val localDate = LocalDate.of(2017, 6, 16)


  def setupUI(): Unit = {
    jQuery("body").append(s"<p>Cities: ${cities.mkString(", ")}</p>")
    jQuery("body").append(s"<p>Date: $localDate</p>")
    onUIready()
  }

  def handleResults(results: Seq[HistoryResponse]): Unit = {
    val day1 = results.head.days.flatMap(_.dataPoints)
    val day2 = results.last.days.flatMap(_.dataPoints)
    jQuery("body").append(s"<p>Day1: $day1</p>")
    jQuery("body").append(s"<p>Day2: $day2</p>")
    Unit
  }

  def onUIready(): Unit = {

    // TODO: hardcode
    val citiesNames = Seq("Moscow", "Saint-Petersburg")
    val date = LocalDate.of(2017, 6, 17)
    val days = 1

    val future = for {
      cities <- weatherApiClient.locations(citiesNames)
      cityHistories <- weatherApiClient.history(cities, date, days)

    } yield cityHistories

    future.onComplete {
      case Failure(e) => println(e)
      case Success(cityHistories) => D3Graph.plot(cityHistories)
    }

  }


  def main(args: Array[String]): Unit = {
    println("WeatherWebApp is up and running")
    jQuery(() => setupUI())
  }
}
