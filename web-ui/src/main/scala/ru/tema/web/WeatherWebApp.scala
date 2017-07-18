package ru.tema.web

import java.time.LocalDate

import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.{ Date, Dictionary }
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3

import scala.scalajs.js
//import scala.scalajs.js._
import org.scalajs.dom

object WeatherWebApp {

  trait Data {
    var date: Date
    var close: Double
  }

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
    zzz()
  }

  def handleResults(results: Seq[HistoryResponse]) = {
    val day1 = results.head.days.flatMap(_.dataPoints)
    val day2 = results.last.days.flatMap(_.dataPoints)
    jQuery("body").append(s"<p>Day1: $day1</p>")
    jQuery("body").append(s"<p>Day2: $day2</p>")
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

  def zzz(): Unit = {

    object margin {
      val top = 30
      val right = 20
      val bottom = 30
      val left = 50
    }

    val width = 600 - margin.left - margin.right
    val height = 270 - margin.top - margin.bottom

    val parseDate = d3.time.format("%d-%b-%y").parse(_)

    val x = d3.time.scale().range(js.Array(0, width))
    val y = d3.scale.linear().range(js.Array(height, 0))

    val xAxis = d3.svg.axis().scale(x)
      .orient("bottom").ticks(5)
    val yAxis = d3.svg.axis().scale(y)
      .orient("left").ticks(5)

    val valueline = d3.svg.line[Data]()
      .x { (d: Data) => x(d.date) }
      .y { (d: Data) => y(d.close) }

    val svg = d3.select("body")
      .append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform",
        "translate(" + margin.left + "," + margin.top + ")")

    d3.csv("data.csv", { (error: Any, rawdata: js.Array[Dictionary[String]]) =>

      val data: js.Array[Data] = rawdata.map { record =>
        object d extends Data {
          var date = parseDate(record("date"))
          var close = record("close").toDouble
        }
        d
      }

      val z1z2 = d3.extent(data.map(_.date))
      val minx = z1z2._1
      val maxx = z1z2._2
      x.domain(js.Array(minx, maxx))

      val maxy = d3.max(data.map(_.close))
      y.domain(js.Array(0, maxy))

      svg.append("path")
        .attr("class", "line")
        .datum(data)  // This is needed to reference the actual data
        .attr("d", valueline)

//      svg.append("path")
//        .attr("class", "line")
//        .attr("d", valueline(data))

      svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis)

      svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)

      ()
    })
  }


  def main(args: Array[String]): Unit = {
    println("WeatherWebApp is up and running")
    jQuery(() => setupUI())
  }
}
