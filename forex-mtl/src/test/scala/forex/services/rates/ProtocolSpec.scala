package forex.services.rates

import forex.TestInstances.testOffsetDateTime
import forex.domain.Currency.{EUR, JPY, USD}
import forex.domain.{Price, Rate}
import forex.services.rates.Protocol.GetRatesResponse
import forex.services.rates.ProtocolFixtures.buildOneForgeRateResponse
import io.circe.parser.decode
import org.scalatest.{FreeSpec, Matchers}

class ProtocolSpec extends FreeSpec with Matchers {

  val jsonResponse: String =
    """
      |[
      |    {
      |        "symbol": "EURUSD",
      |        "bid": 1.12133,
      |        "ask": 1.12135,
      |        "price": 1.12134,
      |        "timestamp": 1564790400
      |    },
      |    {
      |        "symbol": "USDJPY",
      |        "bid": 106.1,
      |        "ask": 106.102,
      |        "price": 106.101,
      |        "timestamp": 1564790400
      |    }
      |]
    """.stripMargin

  "decoder" - {
    "should successfully decode GetRatesResponse containing OneForgeRatePair" in {
      val expectedResult = GetRatesResponse(
        Vector(
          buildOneForgeRateResponse(Rate.Pair(EUR, USD), Price(1.12134), testOffsetDateTime),
          buildOneForgeRateResponse(Rate.Pair(USD, JPY), Price(106.101), testOffsetDateTime)
        )
      )
      decode[GetRatesResponse](jsonResponse) shouldBe Right(expectedResult)
    }
  }
}
