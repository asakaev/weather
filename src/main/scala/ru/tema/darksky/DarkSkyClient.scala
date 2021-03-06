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
case class DataPoint(time: Long, temperature: Double, humidity: Double, windSpeed: Double, windBearing: Double)
case class DataBlock(data: Seq[DataPoint]) // 00:00 to 23:00
case class Response(hourly: DataBlock, timezone: String)

case class Location(lat: Double, lon: Double)


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val dataPoint5 = jsonFormat5(DataPoint)
  implicit val dataBlock1 = jsonFormat1(DataBlock)
  implicit val response2 = jsonFormat2(Response)
}


// TODO:
// system as parameter?
// system.terminate()
// NOT 200 code from API handling
// connection pool to API w limit concurrent connections


/**
  * Dark Sky API (partially implemented)
  */
class DarkSkyClient(apiKey: String) extends DarkSkyService with JsonSupport {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer() // TODO: as arguments?

  private val endpoint = "https://api.darksky.net"
  private val exclude = Seq("currently", "flags", "daily")
  private val units = "si"


  override def history(location: Location, time: LocalDateTime): Future[Response] = {
    val epochSecond = epochTime(time)
    println(s"epoch request: $epochSecond")
    val uri = createUri(location, epochSecond)
    for {
      response <- Http().singleRequest(HttpRequest(uri = uri))
      historyResponse <- Unmarshal(response.entity).to[Response] // TODO: json4s?
    } yield {
      require(historyResponse.hourly.data.length == 24, "should be 24 data points per day")
      historyResponse
    }
  }

  private def createUri(location: Location, epochSecond: Long) = {
    s"$endpoint/forecast/$apiKey/${location.lat},${location.lon},$epochSecond" +
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
