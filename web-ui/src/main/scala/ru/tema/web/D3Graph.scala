package ru.tema.web

import org.scalajs.dom
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.scale.Linear
import org.singlespaced.d3js.svg.Line
import org.singlespaced.d3js.{ Selection, d3 }

import scala.scalajs.js
import scala.scalajs.js.JSConverters._


case class TempPoint(time: Long, temp: Double)
case class CityWithTimePoints(cityName: String, tempPoints: Seq[TempPoint])

case class Point[T](x: T, y: T)
case class Label(text: String, point: Point[Double])
case class TempLine(tempPoints: Seq[TempPoint], cityLabel: Label)


object D3Graph {

  object margin {
    val top = 30
    val right = 100
    val bottom = 30
    val left = 50
  }

  val windowWidth = dom.window.innerWidth - 30 // TODO: fix

  val width = windowWidth - margin.left - margin.right
  val height = 270 - margin.top - margin.bottom


  def render(cityHistories: Seq[CityHistory]): Unit = {
    val x = d3.time.scale().range(Seq(0.0, width).toJSArray)
    val y = d3.scale.linear().range(Seq(height, 0.0).toJSArray)

    val xAxis = d3.svg.axis().scale(x).orient("bottom").ticks(5)
    val yAxis = d3.svg.axis().scale(y).orient("left").ticks(5)

    val valueLine = d3.svg.line[TempPoint]()
      .x { tp: TempPoint => x(unixEpochtoJsDate(tp.time)) }
      .y { tp: TempPoint => y(tp.temp) }

    val svg = d3.select("body")
      .append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", s"translate(${margin.left},${margin.top})")

    // scale
    val allDataPoints = cityHistories
      .map(_.historyResponse)
      .flatMap(_.days)
      .flatMap(_.dataPoints)

    def timeRange(min: Long, max: Long) = Seq(min, max).map(unixEpochtoJsDate)
    def tempsRange(min: Double, max: Double) = Seq(min, max)

    val timestamps = allDataPoints.map(_.time)
    val temps = allDataPoints.map(_.temperature)

    x.domain(timeRange(timestamps.min, timestamps.max).toJSArray)
    y.domain(tempsRange(temps.min, temps.max).toJSArray)

    // axes
    svg.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)

    svg.append("g")
      .attr("class", "y axis")
      .call(yAxis)


    def cityDataPoints(historyResponse: HistoryResponse): Seq[TempPoint] =
      historyResponse.days.flatMap(_.dataPoints).map(dp => TempPoint(dp.time, dp.temperature))

    def toCityWithTimePoints(ch: CityHistory) = {
      CityWithTimePoints(ch.city.title, cityDataPoints(ch.historyResponse))
    }

    val citiesData = cityHistories.map(toCityWithTimePoints)

    citiesData.zipWithIndex.foreach {
      case (points, idx) => appendCityLine(points, valueLine, svg, y, idx, citiesData.length)
    }

  }

  private def cityData(
    x: CityWithTimePoints,
    y: Linear[Double, Double],
  ): TempLine = {
    val lastTemp = x.tempPoints.maxBy(_.time).temp
    val xy = Point(width + 3, y(lastTemp))
    TempLine(x.tempPoints.sortBy(_.time), Label(x.cityName, xy))
  }

  private def appendCityLine(
    cityWithTimePoints: CityWithTimePoints,
    valueLine: Line[TempPoint],
    svg: Selection[EventTarget],
    y: Linear[Double, Double],
    index: Int,
    totalOfLines: Int
  ): Selection[EventTarget] = {
    val tempLine = cityData(cityWithTimePoints, y)
    val cityLabelPoint = tempLine.cityLabel.point

    val colorVal = index.toDouble / (totalOfLines - 1)
    val color = d3.interpolateRgb("steelblue", "brown")(colorVal)

    svg.append("path")
      .attr("class", "line")
      .datum(tempLine.tempPoints.toJSArray)
      .attr("d", valueLine)
      .style("stroke", color)

    svg.append("text")
      .attr("transform", s"translate(${cityLabelPoint.x},${cityLabelPoint.y})")
      .attr("dy", ".35em")
      .attr("text-anchor", "start")
      .style("fill", color)
      .text(tempLine.cityLabel.text)

    svg
  }

  private def unixEpochtoJsDate(x: Long) = new js.Date(x * 1000)
}
