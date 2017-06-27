package ru.tema

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ru.tema.api.WeatherApi
import ru.tema.darksky.{ DarkSkyClient, Location, Observation }

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }


object Application extends App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  val apiKey = ""
  val darkSkyClient = new DarkSkyClient(apiKey)
  val weatherApi = new WeatherApi(darkSkyClient)

  def getObservations(lat: Double, lon: Double, fromTime: Long, days: Int): Future[Seq[Observation]] = {
    weatherApi.getHistory(Location(lat, lon), fromTime, days)
  }

  val route: Route =
    get {
      path("gethistory") {
        parameters('lat.as[Double], 'lon.as[Double], 'fromtime.as[Long], 'days.as[Int]) { (lat, lon, fromTime, days) =>
          println(s"lan: $lat, lon: $lon, from: $fromTime, days: $days")

          val observations: Future[Seq[Observation]] = getObservations(lat, lon, fromTime, days)

          onSuccess(observations) { obs =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, obs.toString))
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  Await.result(bindingFuture, Duration.Inf)
}
