package ru.tema.web

import org.scalajs.jquery.{ JQuery, jQuery }


object StatsView {

  def render(cityHistories: Seq[CityHistory]): Unit = {
    cityHistories.foreach(renderCityHistory)
  }

  private def renderCityHistory(cityHistory: CityHistory) = {
    val title = cityHistory.city.title
    val days = cityHistory.historyResponse.days
    appendCity(title, days)
  }

  private def appendDay(day: Day, parent: JQuery) = {
    parent.append(s"<h3>Date: ${day.zonedDateTime}</h3>")

    val tables = Seq(
      detailedStatsTable(day.dayStats.day, "Day (6AM to 6PM)"),
      detailedStatsTable(day.dayStats.night, "Night (Before 6AM and after 6PM)"),
      detailedStatsTable(day.dayStats.twentyFourHours, "24 hours")
    )

    tables.foreach(parent.append(_))
  }

  private def detailedStatsTable(detailedStats: DetailedStats, caption: String) = {
    val temp = statsTableHtml(detailedStats.temp)
    val humidity = statsTableHtml(detailedStats.humidity)
    val windBearing = statsTableHtml(detailedStats.windBearing)
    val windStrength = statsTableHtml(detailedStats.windStrength)

    s"""
       |<table style="width:100%">
       |<caption><b>$caption</b></caption>
       |  <tr><th>Temp</th><th>Humidity</th><th>WindBearing</th><th>WindStrength</th></tr>
       |    <tr>
       |      <td>$temp</td>
       |      <td>$humidity</td>
       |      <td>$windBearing</td>
       |      <td>$windStrength</td>
       |    </tr>
       |</table>
    """.stripMargin
  }

  private def statsTableHtml(stats: Stats): String = {
    val scale = 4
    s"""
      |<table style="width:100%">
      |  <tr><th>Min</th><th>Max</th><th>Median</th><th>StdDev</th></tr>
      |    <tr>
      |      <td>${round(stats.min, scale)}</td>
      |      <td>${round(stats.max, scale)}</td>
      |      <td>${round(stats.median, scale)}</td>
      |      <td>${round(stats.standardDeviation, scale)}</td>
      |    </tr>
      |</table>
    """.stripMargin
  }

  private def appendCity(cityName: String, days: Seq[Day]) = {
    val cityContainer = jQuery("<div class='city'></div>")
    jQuery("body").append(cityContainer)
    cityContainer.append(s"<h2>$cityName</h2>")
    val statsContainer = jQuery("<div class='stats'></div>")
    cityContainer.append(statsContainer)
    days.foreach(appendDay(_, statsContainer))
  }

  private def round(x: Double, scale: Int) =
    BigDecimal(1.23456789).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble

}
