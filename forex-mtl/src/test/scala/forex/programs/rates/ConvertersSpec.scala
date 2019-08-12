package forex.programs.rates

import forex.domain.{Rate, RateFixtures}
import forex.programs.rates.Converters._
import org.scalatest.{FlatSpec, Matchers}

class ConvertersSpec extends FlatSpec with Matchers {

  "toRatesMap" should "successfully convert rates into a map with the pair as key" in {
    val rates = Vector(RateFixtures.buildRate())
    val expectedRate = rates.head
    val expectedResult =
      Map(s"${expectedRate.pair.asString}" -> Rate(expectedRate.pair, expectedRate.price, expectedRate.timestamp))
    rates.toRatesMap shouldBe expectedResult
  }
}
