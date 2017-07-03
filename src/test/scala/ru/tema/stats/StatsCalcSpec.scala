package ru.tema.stats

import org.scalatest.FunSpec


class StatsCalcSpec extends FunSpec {

  val sc = new StatsCalc
  val dataSet1 = Seq[Double](2, 4, 4, 4, 5, 5, 7, 9)
  val dataSet2 = Seq[Double](206, 76, -224, 36, -94)

  describe("StatsCalc") {
    it("should calculate stDev") {
      assert(sc.stdDev(dataSet1) === 2.0)
      assert(sc.stdDev(dataSet2) === 147.32277488562318)
    }

    it("should throw ex when stDev is invoked on empty dataset") {
      assertThrows[IllegalArgumentException] {
        sc.stdDev(Seq())
      }
    }

  }
}