package forex.services.rates

import forex.TestInstances.testOffsetDateTime
import forex.domain.Currency.{EUR, JPY, USD}
import forex.domain.{Price, Rate}
import forex.services.rates.Protocol.GetQuotaResponse.OneForgeQuotaResponse
import forex.services.rates.Protocol.{GetQuotaResponse, GetRatesResponse}
import forex.services.rates.ProtocolFixtures.buildOneForgeRateResponse
import io.circe.parser.decode
import org.scalatest.{FreeSpec, Matchers}

class ProtocolSpec extends FreeSpec with Matchers {

  "decodeOneForgeRateResponse" - {
    val jsonOneForgeRatesResponse: String =
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

    "should successfully decode GetRatesResponse containing OneForgeRatePair" in {
      val expectedResult = GetRatesResponse(
        Vector(
          buildOneForgeRateResponse(Rate.Pair(EUR, USD), Price(1.12134), testOffsetDateTime),
          buildOneForgeRateResponse(Rate.Pair(USD, JPY), Price(106.101), testOffsetDateTime)
        )
      )
      decode[GetRatesResponse](jsonOneForgeRatesResponse) shouldBe Right(expectedResult)
    }
  }

  "decodeOneForgeQuota" - {
    val jsonOneForgeQuotaResponse: String =
      """
        |{
        |     "quota_used": 5,
        |     "quota_limit": 2,
        |     "quota_remaining": 3,
        |     "hours_until_reset": 12
        |}
      """.stripMargin

    "should successfully decode OneForgeQuota" in {
      val expectedResult = GetQuotaResponse(OneForgeQuotaResponse(remaining = 3, hoursUntilReset = 12))
      decode[GetQuotaResponse](jsonOneForgeQuotaResponse) shouldBe Right(expectedResult)
    }
  }
}
