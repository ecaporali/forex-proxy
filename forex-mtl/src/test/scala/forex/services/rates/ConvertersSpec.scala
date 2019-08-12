package forex.services.rates

import forex.domain.Rate
import forex.services.rates.Converters._
import org.scalatest.{FlatSpec, Matchers}

class ConvertersSpec extends FlatSpec with Matchers {

  "RateResponse converters toRateEntry" should "successfully convert a RateResponse into a pair" in {
    val getRateResponse = ProtocolFixtures.buildGetRatesResponse()
    val expectedRate    = getRateResponse.rates.head
    val expectedResult  = Seq(Rate(expectedRate.pair, expectedRate.price, expectedRate.timestamp))

    getRateResponse.toRates shouldBe expectedResult
  }
}
