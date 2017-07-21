package ru.tema.web

import java.time.LocalDate

import org.scalajs.jquery.jQuery
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{ Failure, Success }


object WeatherWebApp {

  private val endpoint = "http://localhost:8080"
  private val weatherApiClient = new WeatherApiClient(endpoint)

  // TODO: hardcode
  private val cities = Seq("Saint-Petersburg", "Moscow")
  private val localDate = LocalDate.of(2017, 6, 16)


  def setupUI(): Unit = {
    jQuery("body").append(s"<p>Cities: ${cities.mkString(", ")}</p>")
    jQuery("body").append(s"<p>Date: $localDate</p>")
    jQuery("body").append("""<button id="locations-btn" type="button">Locations</button>""")
    jQuery("#locations-btn").click(() => onLocationsBtn())
    drawD3()
  }

  def handleResults(results: Seq[HistoryResponse]): Unit = {
    val day1 = results.head.days.flatMap(_.dataPoints)
    val day2 = results.last.days.flatMap(_.dataPoints)
    jQuery("body").append(s"<p>Day1: $day1</p>")
    jQuery("body").append(s"<p>Day2: $day2</p>")
    Unit
  }

  def onLocationsBtn(): Unit = {
    val result = for {
      citiesWithLocation <- weatherApiClient.locations(cities)
      results <- {
        jQuery("body").append(s"<p>$citiesWithLocation</p>")
        val locations = citiesWithLocation.map(_.location)
        weatherApiClient.history(locations, localDate, 1)
      }
    } yield {
      handleResults(results)
      Unit
    }

    result.recover {
      case e: Throwable =>
        e.printStackTrace()
        jQuery("body").append(s"<p>ERROR: ${e.getMessage}</p>")
    }
  }

  case class Zzz(
    datesRange: js.Array[js.Date],
    allDataPoints: Seq[DataPoint],
    city1Points: js.Array[DataPoint], // hack
    city2Points: js.Array[DataPoint] // hack
  )

  def prepareData(historyResponses: Seq[HistoryResponse]) = {
    // for each
    val city1 = historyResponses.head // TODO: warn
    val city2 = historyResponses.last // TODO: warn

    val dataPoints1: Seq[DataPoint] = city1.days.flatMap(_.dataPoints)
    val dataPoints2: Seq[DataPoint] = city2.days.flatMap(_.dataPoints)

    // calc scale
    val allDataPoints = historyResponses.flatMap(_.days).flatMap(_.dataPoints)
    val dates = allDataPoints.map(_.time)
    println(s"dates: $dates")

    val datesRange = Seq(dates.min, dates.max)
      .map(_ * 1000)
      .map(new js.Date(_))

    Zzz(datesRange.toJSArray, allDataPoints, dataPoints1.toJSArray, dataPoints2.toJSArray)
  }

  def drawD3(): Unit = {

    object margin {
      val top = 30
      val right = 20
      val bottom = 30
      val left = 50
    }

    val width = 600 - margin.left - margin.right
    val height = 270 - margin.top - margin.bottom

    // val parseDate = d3.time.format("%d-%b-%y").parse(_)
    // .map(epoch => new js.Date(epoch))

    val x = d3.time.scale().range(js.Array(0, width))
    val y = d3.scale.linear().range(js.Array(height, 0))

    val xAxis = d3.svg.axis().scale(x)
      .orient("bottom").ticks(5)
    val yAxis = d3.svg.axis().scale(y)
      .orient("left").ticks(5)

    val valueline = d3.svg.line[DataPoint]()
      .x { (d: DataPoint) => x(d.time * 1000) } // hack
      .y { (d: DataPoint) => y(d.temperature) }

    val svg = d3.select("body")
      .append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform",
        "translate(" + margin.left + "," + margin.top + ")")

    // ----------------------------------------------------------

    // TODO: from RPC
    val cities = Seq(
      City("Moscow", Location(55.751244,37.618423)),
      City("Saint-Petersburg", Location(59.8944444,30.2641667))
    )

    val locations = cities.map(_.location)
    val date = LocalDate.of(2017, 6, 17)
    val days = 1

    val eventualHistoryResponses = weatherApiClient.history(locations, date, days)

    eventualHistoryResponses.onComplete {
      case Failure(e) => println(e)
      case Success(historyResponses) =>
        println("------------------")
        println(historyResponses)

        val preparedData = prepareData(historyResponses)
        println(s"preparedData: $preparedData")

        // calc scale
        val allDataPoints = preparedData.allDataPoints
        val dates = allDataPoints.map(_.time)
        println(s"dates: $dates")

        println(s">>>>>>>> datesRange: ${preparedData.datesRange}")
        x.domain(preparedData.datesRange)

        val temps = allDataPoints.map(_.temperature)
        val maxY = temps.max
        val tempsRange = js.Array(0, maxY)
        println(s">>>>>>>> tempsRange: $tempsRange")
        y.domain(tempsRange)


        // city 1
        svg.append("path")
          .attr("class", "line")
          .datum(preparedData.city1Points) // This is needed to reference the actual data
          .attr("d", valueline)

        // city 2
        svg.append("path")
          .attr("class", "line")
          .datum(preparedData.city2Points)
          .attr("d", valueline)

        // axes
        svg.append("g")
          .attr("class", "x axis")
          .attr("transform", "translate(0," + height + ")")
          .call(xAxis)

        svg.append("g")
          .attr("class", "y axis")
          .call(yAxis)

    }

  }


  def main(args: Array[String]): Unit = {
    println("WeatherWebApp is up and running")
    jQuery(() => setupUI())
  }
}
