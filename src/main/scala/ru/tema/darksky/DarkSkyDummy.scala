package ru.tema.darksky

import java.time.LocalDateTime

import scala.concurrent.Future
import scala.util.Random


class DarkSkyDummy extends DarkSkyService {
  override def history(location: Location, time: LocalDateTime): Future[Response] = {
    println(s"Dummy history for $location at $time")
    def toDataPoint(x: Int) = DataPoint(unixEpochDay(x), tempRand, rand, rand, rand)
    val dataPoints = 0 until 24 map toDataPoint
    Future.successful(Response(DataBlock(dataPoints), "Europe/Moscow"))
  }

  private def tempRand = Math.abs(Random.nextLong) % 24
  private def rand = Random.nextLong
  private def unixEpochDay(x: Int) = x * 24 * 60 * 60
}
