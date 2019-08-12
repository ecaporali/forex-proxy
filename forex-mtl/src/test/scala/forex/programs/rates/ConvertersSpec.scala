package forex.programs.rates

import forex.domain.{ Rate, RateFixtures }
import forex.programs.rates.Converters._
import org.scalatest.{ FlatSpec, Matchers }

class ConvertersSpec extends FlatSpec with Matchers {

  "RateResponse converters toRateEntry" should "successfully convert a RateResponse into a pair" in {
    val rateResponse = Vector(RateFixtures.buildRate())
    val expectedRate = rateResponse.head
    val expectedResult =
      Map(s"${expectedRate.pair.asString}" -> Rate(expectedRate.pair, expectedRate.price, expectedRate.timestamp))
    rateResponse.toRatesMap shouldBe expectedResult
  }
}
