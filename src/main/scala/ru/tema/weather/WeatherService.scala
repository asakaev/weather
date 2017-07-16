package ru.tema.weather

import java.time._

import ru.tema.darksky.{ DarkSkyClient, DataPoint, Location, Response }
import ru.tema.stats.StatsCalc

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// TODO: IO execution context. not global


case class Stats(standardDeviation: Double, median: Double, min: Double, max: Double)
case class DetailedStats(temp: Stats, humidity: Stats, windStrength: Stats, windBearing: Stats)
case class DayStats(twentyFourHours: DetailedStats, day: DetailedStats, night: DetailedStats)
case class Day(zonedDateTime: ZonedDateTime, dataPoints: Seq[DataPoint], dayStats: DayStats)

case class DayNightHours(dayHours: Seq[DataPoint], nightHours: Seq[DataPoint])
case class HistoryResponse(days: Seq[Day], overallStats: DetailedStats)


class WeatherService(darkSkyClient: DarkSkyClient, statsCalc: StatsCalc) {

  def getHistory(location: Location, fromTime: LocalDate, days: Int): Future[HistoryResponse] = {
    val localDateTimes = localDateTimeSeq(fromTime, days)
    println(s"localDateTimes: $localDateTimes")

    val futures = localDateTimes.map(ldt => darkSkyClient.history(location, ldt))
    for {
      historyResponses <- Future.sequence(futures)
    } yield createResponse(historyResponses)
  }

  private def createResponse(historyResponses: Seq[Response]) = {
    val daysResult = historyResponses.map(response => {
      val dataPoints = response.hourly.data
      val dataPoint = dataPoints.head
      val zdt = zonedDateTime(dataPoint.time, response.timezone) // TODO: unsafe
      Day(zdt, dataPoints, dayStats(dataPoints))
    })

    val allDataPoints = historyResponses.flatMap(_.hourly.data).sortBy(_.time)
    println(s"> All DataPoints: $allDataPoints")
    val stats = detailedStats(allDataPoints)
    println(s"> stats: $stats")
    
    HistoryResponse(daysResult, detailedStats(allDataPoints))
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

  private def dayStats(dataPoints: Seq[DataPoint]): DayStats = {
    val hours = splitDayNightHours(dataPoints)
    DayStats(detailedStats(dataPoints), detailedStats(hours.dayHours), detailedStats(hours.nightHours))
  }

  private def splitDayNightHours(dataPoints: Seq[DataPoint]) = {
    require(dataPoints.length == 24)
    val buckets = dataPoints.sortBy(_.time).grouped(6).toList
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

  private def zonedDateTime(epochSecond: Long, zoneId: String): ZonedDateTime = {
    Instant.ofEpochSecond(epochSecond).atZone(ZoneId.of(zoneId))
  }

}
