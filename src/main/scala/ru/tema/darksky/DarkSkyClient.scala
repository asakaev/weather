package ru.tema.darksky

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// domain model
case class Hour(time: Double, temperature: Double, humidity: Double, windSpeed: Double, windBearing: Double)
case class Hourly(data: Seq[Hour])
case class HistoryResponse(hourly: Hourly)

case class Location(lat: Double, lon: Double)


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val hour = jsonFormat5(Hour)
  implicit val hourly = jsonFormat1(Hourly)
  implicit val historyResponse = jsonFormat1(HistoryResponse)
}


// TODO:
// system as parameter?
// system.terminate()
// NOT 200 code from API handling
// connection pool to API w limit concurrent connections


/**
  * Dark Sky API (partially implemented)
  */
class DarkSkyClient(apiKey: String) extends JsonSupport {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val endpoint = "https://api.darksky.net"
  private val exclude = Seq("currently", "flags", "daily")

  /**
    * A Time Machine Request returns the observed (in the past) or forecasted (in the future)
    * hour-by-hour weather and daily weather conditions for a particular date.
    */
  def history(location: Location, time: Long): Future[HistoryResponse] = {
    val uri = createUri(location, time)
    for {
      response <- Http().singleRequest(HttpRequest(uri = uri))
      historyResponse <- Unmarshal(response.entity).to[HistoryResponse]
    } yield historyResponse
  }

  private def createUri(location: Location, time: Long) = {
    s"$endpoint/forecast/$apiKey/${location.lat},${location.lon},$time?exclude=${exclude.mkString(",")}"
  }
}
