package ru.tema

import org.scalajs.jquery.jQuery


object WeatherWebApp {

  def setupUI(): Unit = {
    jQuery("#click-me-button").click(() => addClickedMessage())
    jQuery("body").append("<p>Hello World</p>")
  }

  def addClickedMessage(): Unit = {
    jQuery("body").append("<p>[message]</p>")
  }

  def main(args: Array[String]): Unit = {
    println("WeatherWebApp logs")
    jQuery(() => setupUI())
  }
}
