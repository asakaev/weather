package ru.tema.web

import com.zoepepper.facades.jsjoda.LocalDate
import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }


object WeatherWebApp {

  private val endpoint = "http://localhost:8080"
  private val weatherApiClient = new WeatherApiClient(endpoint)

  // TODO: hardcode
  private val cities = Seq("Saint-Petersburg", "Moscow")
  private val date = LocalDate.now().minusDays(1)
  private val days = 7

  def setupUI(): Unit = {
    jQuery("body").append(s"<p>Cities: ${cities.mkString(", ")}</p>")
    jQuery("body").append(s"<p>Date: $date</p>")

    val future = for {
      cities <- weatherApiClient.locations(cities)
      cityHistories <- weatherApiClient.history(cities, date, days)

    } yield cityHistories

    future.onComplete {
      case Failure(e) => println(e)
      case Success(cityHistories) =>
        val reversed = cityHistories.reverse // TODO: fix order
        D3Graph.render(reversed)
        StatsView.render(reversed)
    }
  }


  def main(args: Array[String]): Unit = {
    println("WeatherWebApp is up and running")
    jQuery(() => setupUI())
  }
}
