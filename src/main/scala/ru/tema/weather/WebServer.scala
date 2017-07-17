package ru.tema.weather

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.json4s._
import org.json4s.native.Serialization._
import ru.tema.darksky.Location

import scala.util.{ Failure, Success }


class WebServer(host: String, port: Int, publicApi: PublicApi) {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats


  val locations: Route =
    path("locations") {
      parameters('city.*) { (cities) =>
        val citiesSeq = cities.toSeq
        println(s"cities: $citiesSeq")
        val locationsFuture = publicApi.locations(citiesSeq)
        onSuccess(locationsFuture) { locations =>
          val jsonString = write(locations)
          val httpEntity = HttpEntity(ContentTypes.`application/json`, jsonString)
          complete(httpEntity)
        }
      }
    }

  val history: Route =
    path("history") {
      parameters('lat.as[Double], 'lon.as[Double], 'date.as[String], 'days.as[Int]) { (lat, lon, date, days) =>
        println(s"lat: $lat, lon: $lon, date: $date, days: $days")
        val historyFuture = publicApi.history(Location(lat, lon), date, days)
        onComplete(historyFuture) {
          case Success(historyResponse) =>
            val jsonString = write(historyResponse)
            val httpEntity = HttpEntity(ContentTypes.`application/json`, jsonString)
            complete(httpEntity)
          case Failure(e) =>
            println(s"Internal error: $e")
            // TODO: status 500 error
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "FAIL"))
          //                failWith(new Exception("wtf"))
        }
      }
    }

  val route: Route = get { locations ~ history }
  val corsRoute: Route = cors() { route }

  Http().bindAndHandle(corsRoute, host, port)
}
