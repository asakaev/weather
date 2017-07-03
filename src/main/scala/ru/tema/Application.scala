package ru.tema

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ru.tema.api.{ WeatherApiResponse, WeatherService }
import ru.tema.darksky.{ DarkSkyClient, Location }
import ru.tema.stats.StatsCalc

import scala.concurrent.Future


object Application extends App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  val apiKey = "90ed629753ab611b3c77e053ba8584df"
  val darkSkyClient = new DarkSkyClient(apiKey)
  val statsCalc = new StatsCalc
  val weatherApi = new WeatherService(darkSkyClient, statsCalc)

  def getObservations(lat: Double, lon: Double, date: String, days: Int): Future[WeatherApiResponse] = {
    val parsedDate = time(date)
    println(s"parsed date: $parsedDate")
    weatherApi.getHistory(Location(lat, lon), parsedDate, days)
  }

  private def time(str: String): LocalDate = {
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    LocalDate.parse(str, formatter)
  }

  val route: Route =
    get {
      path("gethistory") {
        parameters('lat.as[Double], 'lon.as[Double], 'date.as[String], 'days.as[Int]) { (lat, lon, date, days) =>
          println(s"lan: $lat, lon: $lon, date: $date, days: $days")

          val observations: Future[WeatherApiResponse] = getObservations(lat, lon, date, days)

          onSuccess(observations) { obs =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, obs.toString))
          }
        }
      }
    }

  Http().bindAndHandle(route, "localhost", 8080)
}
