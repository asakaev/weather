package ru.tema

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import ru.tema.darksky.{ DarkSkyClient, Location }
import ru.tema.repository.CitiesRepo
import ru.tema.stats.StatsCalc
import ru.tema.weather.{ City, PublicApi, WeatherService }
import spray.json.DefaultJsonProtocol


object MyJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val locationFormat = jsonFormat2(Location.apply)
  implicit val cityFormat = jsonFormat2(City.apply)
}


object Application extends App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  val conf = ConfigFactory.load
  val apiKey = conf.getString("dark-sky.api-key")
  val host = conf.getString("web-server.host")
  val port = conf.getInt("web-server.port")

  val darkSkyClient = new DarkSkyClient(apiKey)
  val publicApi = new PublicApi(
    new WeatherService(darkSkyClient, StatsCalc),
    new CitiesRepo
  )

  import MyJsonProtocol._

  val route: Route =
    get {
      path("locations") {
        parameters('city.*) { (cities) =>
          val citiesSeq = cities.toSeq
          println(s"cities: $citiesSeq")
          val locationsFuture = publicApi.locations(citiesSeq)
          onSuccess(locationsFuture) { locations =>
            complete(locations)
          }
        }
      } ~
      path("history") {
        parameters('lat.as[Double], 'lon.as[Double], 'date.as[String], 'days.as[Int]) { (lat, lon, date, days) =>
          println(s"lat: $lat, lon: $lon, date: $date, days: $days")
          val historyFuture = publicApi.history(lat, lon, date, days)
          onSuccess(historyFuture) { response =>
            // TODO: JSON response
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, response.toString))
          }
        }
      }
    }

  Http().bindAndHandle(route, host, port)
}
