package ru.tema.api

import java.time.{ LocalDate, ZoneOffset, ZonedDateTime }

import ru.tema.darksky.{ DarkSkyClient, Location, Observation }
import ru.tema.stats.StatsCalc

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// TODO: IO execution context. not global


case class Stats(standardDeviation: Double, median: Double, min: Double, max: Double)
case class DetailedStats(temp: Stats, humidity: Stats, windStrength: Stats, windBearing: Stats)
case class DayStats(twentyFourHours: DetailedStats, day: DetailedStats, night: DetailedStats)
case class Day(observations: Seq[Observation], dayStats: DayStats)

case class WeatherApiResponse(days: Seq[Day], overallStats: DetailedStats)


case class DayNightHours(dayHours: Seq[Observation], nightHours: Seq[Observation])


// draft
class WeatherService(darkSkyClient: DarkSkyClient, sc: StatsCalc) {

  def getHistory(location: Location, fromTime: LocalDate, days: Int): Future[WeatherApiResponse] = {
    val endOfTheDayTimestamp = endOfTheDay(fromTime)
    println(s"endOfTheDay: $endOfTheDayTimestamp")

    val timestamps = timestampsSeq(endOfTheDayTimestamp, days)
    println(s"timestamps: $timestamps")

    val futures = timestamps.map(ts => darkSkyClient.history(location, ts).map(_.hourly.data))
    for {
      daysObservations <- Future.sequence(futures)
    } yield {
      val daysResult = daysObservations.map(obs => Day(obs, dayStats(obs)))
      val allObservations = daysObservations.flatten
      WeatherApiResponse(daysResult, detailedStats(allObservations))
    }
  }


  private def stats(series: Seq[Double]): Stats = {
    Stats(sc.stdDev(series), sc.median(series), sc.min(series), sc.max(series))
  }

  private def detailedStats(observations: Seq[Observation]): DetailedStats = {
    val temp = observations.map(_.temperature)
    val humidity = observations.map(_.humidity)
    val windSpeed = observations.map(_.windSpeed)
    val windBearing = observations.map(_.windBearing)
    DetailedStats(stats(temp), stats(humidity), stats(windSpeed), stats(windBearing))
  }

  private def dayStats(observations: Seq[Observation]): DayStats = {
    val hours = splitDayNightHours(observations)
    DayStats(detailedStats(observations), detailedStats(hours.dayHours), detailedStats(hours.nightHours))
  }

  private def splitDayNightHours(observations: Seq[Observation]) = {
    require(observations.length == 24)
    val buckets = observations.sortBy(_.time).grouped(6).toList
    val night = buckets(0)
    val morning = buckets(1)
    val afternoon = buckets(2)
    val evening = buckets(3)
    DayNightHours(morning ++ afternoon, night ++ evening)
  }


  // time
  private def endOfTheDay(date: LocalDate): Long = {
    ZonedDateTime
      .of(date.getYear, date.getMonthValue, date.getDayOfMonth, 23, 59, 59, 0, ZoneOffset.UTC)
      .toEpochSecond
  }

  // TODO: fix and test
  private def timestampsSeq(fromTime: Long, days: Int): Seq[Long] = {
    val day = 24 * 60 * 60 * 1000
    0 until days map (x => fromTime - x * day)
  }

}
