package forex.programs.rates

import forex.domain.{Rate, RateFixtures}
import forex.programs.rates.Converters._
import org.scalatest.{FlatSpec, Matchers}

class ConvertersSpec extends FlatSpec with Matchers {

  "asRatesMap" should "successfully convert rates into a map with the pair as key" in {
    val rates        = List(RateFixtures.buildRate())
    val expectedRate = rates.head
    val expectedResult =
      Map(expectedRate.pair -> Rate(expectedRate.pair, expectedRate.price, expectedRate.timestamp))
    rates.asRatesMap shouldBe expectedResult
  }
}
