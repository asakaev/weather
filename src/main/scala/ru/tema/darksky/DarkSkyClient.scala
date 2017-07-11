package ru.tema.darksky

import java.time.{ LocalDateTime, ZoneOffset, ZonedDateTime }

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
case class Observation(time: Long, temperature: Double, humidity: Double, windSpeed: Double, windBearing: Double)
case class Hourly(data: Seq[Observation]) // 00:00 to 23:00
case class HistoryResponse(hourly: Hourly, timezone: String, offset: Int) // TODO: TZ or offset remove?

case class Location(lat: Double, lon: Double)


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val observation = jsonFormat5(Observation)
  implicit val hourly = jsonFormat1(Hourly)
  implicit val historyResponse = jsonFormat3(HistoryResponse)
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
  implicit val materializer = ActorMaterializer() // TODO: as arguments?

  private val endpoint = "https://api.darksky.net"
  private val exclude = Seq("currently", "flags", "daily")
  private val units = "si"

  /**
    * A Time Machine Request returns the observed (in the past) or forecasted (in the future)
    * hour-by-hour weather and daily weather conditions for a particular date.
    *
    * @param location Latitude and longitude (in decimal degrees)
    * @param time UNIX time (timezone should be omitted to refer to local time for the location being requested)
    * @return
    */
  def history(location: Location, time: LocalDateTime): Future[HistoryResponse] = {
    val uri = createUri(location, epochTime(time))
    for {
      response <- Http().singleRequest(HttpRequest(uri = uri))
      historyResponse <- Unmarshal(response.entity).to[HistoryResponse]
    } yield historyResponse
  }

  private def createUri(location: Location, unixTime: Long) = {
    s"$endpoint/forecast/$apiKey/${location.lat},${location.lon},$unixTime" +
      s"?exclude=${exclude.mkString(",")}&units=$units"
  }

  /**
    * The timezone is only used for determining the time of the request;
    * the response will always be relative to the local time zone.
    */
  private def epochTime(time: LocalDateTime): Long = {
    ZonedDateTime.of(time, ZoneOffset.UTC).toEpochSecond
  }
}
