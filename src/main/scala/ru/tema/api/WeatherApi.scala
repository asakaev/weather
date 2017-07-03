package ru.tema.api

import java.time.{ LocalDate, ZoneOffset, ZonedDateTime }

import ru.tema.darksky.{ DarkSkyClient, Location, Observation }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// TODO: IO execution context. not global


case class Stats(standardDeviation: Double, median: Double, min: Double, max: Double)
case class DetailedStats(temp: Stats, humidity: Stats, windStrength: Stats, windBearing: Stats)
case class DayStats(twentyFourHours: DetailedStats, day: DetailedStats, night: DetailedStats)
case class Day(observations: Seq[Observation], dayStats: DayStats)

case class WeatherApiResponse(days: Seq[Day], overallStats: DetailedStats)


// draft
class WeatherApi(darkSkyClient: DarkSkyClient) {

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
    Stats(standardDeviation(series), median(series), min(series), max(series))
  }

  private def detailedStats(observations: Seq[Observation]): DetailedStats = {
    val temp = observations.map(_.temperature)
    val humidity = observations.map(_.humidity)
    val windSpeed = observations.map(_.windSpeed)
    val windBearing = observations.map(_.windBearing)
    DetailedStats(stats(temp), stats(humidity), stats(windSpeed), stats(windBearing))
  }

  private def dayStats(observations: Seq[Observation]): DayStats = {
    val half = observations.length / 2
    val (dayz, nightz) = observations.splitAt(half) // TODO: day/night groupBy hours (22 - 06)?
    DayStats(detailedStats(observations), detailedStats(dayz), detailedStats(nightz))
  }


  // math
  // TODO: implement
  def standardDeviation(series: Seq[Double]): Double = 0
  def median(series: Seq[Double]): Double = 0
  def min(series: Seq[Double]): Double = 0
  def max(series: Seq[Double]): Double = 0


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
