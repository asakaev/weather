package ru.tema

import org.scalajs.dom
import dom.document

import scala.scalajs.js.annotation.JSExportTopLevel


object WeatherWebApp {

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    val textNode = document.createTextNode(text)
    parNode.appendChild(textNode)
    targetNode.appendChild(parNode)
  }

  @JSExportTopLevel("addClickedMessage")
  def addClickedMessage(): Unit = {
    appendPar(document.body, "You clicked the button!")
  }

  def main(args: Array[String]): Unit = {
    println("WeatherWebApp logs")
    appendPar(document.body, "WeatherWebApp init")
  }
}
