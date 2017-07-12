package ru.tema.weather

import java.time._

import ru.tema.darksky.{ DarkSkyClient, DataPoint, Location }
import ru.tema.stats.StatsCalc

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// TODO: IO execution context. not global


case class Stats(standardDeviation: Double, median: Double, min: Double, max: Double)
case class DetailedStats(temp: Stats, humidity: Stats, windStrength: Stats, windBearing: Stats)
case class DayStats(twentyFourHours: DetailedStats, day: DetailedStats, night: DetailedStats)
case class Day(zonedDateTime: ZonedDateTime, observations: Seq[DataPoint], dayStats: DayStats)

case class HistoryResponse(days: Seq[Day], overallStats: DetailedStats)

case class DayNightHours(dayHours: Seq[DataPoint], nightHours: Seq[DataPoint])


// TODO: draft!
class WeatherService(darkSkyClient: DarkSkyClient, statsCalc: StatsCalc) {

  def getHistory(location: Location, fromTime: LocalDate, days: Int): Future[HistoryResponse] = {
    val localDateTimes = localDateTimeSeq(fromTime, days)
    println(s"localDateTimes: $localDateTimes")

    val futures = localDateTimes.map(ldt => darkSkyClient.history(location, ldt))
    for {
      historyResponses <- Future.sequence(futures)
    } yield {
      val daysResult = historyResponses.map(response => {
        val dataPoints = response.hourly.data
        val dataPoint = dataPoints.head // TODO: exception!
        val zdt = zonedDateTime(dataPoint.time, response.timezone) // TODO: time is wrong here. ONE point only
        Day(zdt, dataPoints, dayStats(dataPoints))
      })

      val dataPoints = historyResponses.flatMap(_.hourly.data)
      val sorted = dataPoints.sortBy(_.time)
      println(s"DataPoints before stats: $dataPoints")
      println(s"DataPoints before stats sorted: $sorted")
      HistoryResponse(daysResult, detailedStats(dataPoints))
    }
  }

  private def stats(series: Seq[Double]): Stats = {
    Stats(statsCalc.stdDev(series), statsCalc.median(series), series.min, series.max)
  }

  private def detailedStats(dataPoints: Seq[DataPoint]): DetailedStats = {
    val temp = dataPoints.map(_.temperature)
    val humidity = dataPoints.map(_.humidity)
    val windSpeed = dataPoints.map(_.windSpeed)
    val windBearing = dataPoints.map(_.windBearing)
    DetailedStats(stats(temp), stats(humidity), stats(windSpeed), stats(windBearing))
  }

  private def dayStats(observations: Seq[DataPoint]): DayStats = {
    val hours = splitDayNightHours(observations)
    DayStats(detailedStats(observations), detailedStats(hours.dayHours), detailedStats(hours.nightHours))
  }

  private def splitDayNightHours(observations: Seq[DataPoint]) = {
    require(observations.length == 24)
    val buckets = observations.sortBy(_.time).grouped(6).toList
    val night = buckets(0)
    val morning = buckets(1)
    val afternoon = buckets(2)
    val evening = buckets(3)
    DayNightHours(morning ++ afternoon, night ++ evening)
  }

  private def localDateTimeSeq(fromTime: LocalDate, days: Int): Seq[LocalDateTime] = {
    (0 until days)
      .map(x => fromTime.minusDays(x))
      .map(ld => LocalDateTime.of(ld, LocalTime.of(0, 0)))
  }

  private def zonedDateTime(time: Long, timezone: String): ZonedDateTime = {
    Instant.ofEpochMilli(time).atZone(ZoneId.of(timezone))
  }

}
