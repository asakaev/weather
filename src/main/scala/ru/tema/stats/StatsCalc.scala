package ru.tema.stats

import java.lang.Math.sqrt


class StatsCalc {

  def stdDev(xs: Seq[Double]): Double = {
    require(xs.nonEmpty)
    val mean = xs.sum / xs.length
    sqrt(xs.map(_ - mean).map(t => t * t).sum / xs.length)
  }

  def median(xs: Seq[Double]): Double = {
    val sortedSeq = xs.sortWith(_ < _)

    if (xs.size % 2 == 1) sortedSeq(sortedSeq.size / 2)
    else {
      val (up, down) = sortedSeq.splitAt(xs.size / 2)
      (up.last + down.head) / 2
    }
  }
}
