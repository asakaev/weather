package ru.tema.api

import ru.tema.darksky.{ DarkSkyClient, Location, Observation }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// TODO: IO execution context. not global

// draft
class WeatherApi(darkSkyClient: DarkSkyClient) {

  def getHistory(location: Location, fromTime: Long, days: Int): Future[Seq[Observation]] = {
    val timestamps = daysList(fromTime, days)
    val futures = timestamps.map(ts => darkSkyClient.history(location, ts).map(_.hourly.data))
    Future.sequence(futures).map(_.flatten) // TODO: sort reversed?
  }

  private def daysList(fromTime: Long, days: Int): Seq[Long] = {
    val day = 24 * 60 * 60 * 1000
    0 until days map (x => fromTime - x * day)
  }

}
