package ru.tema.stats

import org.scalatest.FunSpec
import ru.tema.stats.StatsCalc.{ median, stdDev }


class StatsCalcSpec extends FunSpec {

  describe("StatsCalc") {
    it("should calculate stDev") {
      val dataSet1 = Seq[Double](2, 4, 4, 4, 5, 5, 7, 9)
      val dataSet2 = Seq[Double](206, 76, -224, 36, -94)
      assert(stdDev(dataSet1) === 2.0)
      assert(stdDev(dataSet2) === 147.32277488562318)
    }

    it("should throw ex when stDev is invoked on empty dataset") {
      assertThrows[IllegalArgumentException] {
        stdDev(Seq.empty)
      }
    }

    it("should calculate median") {
      val dataSet1 = Seq[Double](11, 9, 3, 5, 5)
      val dataSet2 = Seq[Double](1, 3, 3, 6, 7, 8, 9)
      assert(median(dataSet1) === 5.0)
      assert(median(dataSet2) === 6.0)
    }

  }
}
