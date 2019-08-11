package forex.programs.rates

import forex.domain.Rate
import forex.programs.rates.Converters._
import forex.services.rates.ProtocolFixtures
import org.scalatest.{ FlatSpec, Matchers }

class ConvertersSpec extends FlatSpec with Matchers {

  "RateResponse converters toRateEntry" should "successfully convert a RateResponse into a pair" in {
    val rateResponse = ProtocolFixtures.buildOneForgeRateResponse()
    val expectedResult =
      (s"${rateResponse.pair.asString}", Rate(rateResponse.pair, rateResponse.price, rateResponse.timestamp))

    rateResponse.toRateEntry shouldBe expectedResult
  }
}
