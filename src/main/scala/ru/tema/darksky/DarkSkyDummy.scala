package ru.tema.darksky

import java.time.{ LocalDateTime, ZoneOffset }

import scala.concurrent.Future
import scala.util.Random


class DarkSkyDummy extends DarkSkyService {
  override def history(location: Location, time: LocalDateTime): Future[Response] = {
    println(s"Dummy history for $location at $time")
    def toTime(x: Int) = time.minusHours(x).toEpochSecond(ZoneOffset.UTC)
    def toDataPoint(x: Int) = DataPoint(toTime(x), tempRand, rand, rand, rand)

    val dataPoints = 0 until 24 map toDataPoint
    println(s"data points: ${dataPoints.length}")
    Future.successful(Response(DataBlock(dataPoints), "Europe/Moscow"))
  }

  private def tempRand = Math.abs(Random.nextLong) % 24
  private def rand = Random.nextLong
}
