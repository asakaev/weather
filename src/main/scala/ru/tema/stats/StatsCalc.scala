package ru.tema.stats

import java.lang.Math.sqrt


class StatsCalc {
  def stdDev(xs: Seq[Double]): Double = {
    require(xs.nonEmpty)
    val mean = xs.sum / xs.length
    sqrt(xs.map(_ - mean).map(t => t * t).sum / xs.length)
  }
  def median(xs: Seq[Double]): Double = xs.sorted.drop(xs.length / 2).head
  def min(xs: Seq[Double]): Double = xs.min
  def max(xs: Seq[Double]): Double = xs.max
}
