package forex.services.rates

import forex.domain.Rate
import forex.services.rates.Converters._
import org.scalatest.{FlatSpec, Matchers}

class ConvertersSpec extends FlatSpec with Matchers {

  "toRates" should "successfully convert a GetRatesResponse into rates" in {
    val getRateResponse = ProtocolFixtures.buildGetRatesResponse()
    val expectedRate    = getRateResponse.rates.head
    val expectedResult  = List(Rate(expectedRate.pair, expectedRate.price, expectedRate.timestamp))

    getRateResponse.toRates shouldBe expectedResult
  }
}
