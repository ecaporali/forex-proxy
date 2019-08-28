package forex.services.rates

import java.time.OffsetDateTime

import forex.TestInstances.testOffsetDateTime
import forex.domain.Currency.{ EUR, USD }
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.rates.Protocol.GetQuotaResponse.OneForgeQuotaResponse
import forex.services.rates.Protocol.GetRatesResponse.OneForgeRateResponse
import forex.services.rates.Protocol.{ GetQuotaResponse, GetRatesResponse }

object ProtocolFixtures {

  def buildGetRatesResponse(
      ratesResponse: List[Protocol.RateResponse] = List(buildOneForgeRateResponse())
  ): GetRatesResponse = GetRatesResponse(ratesResponse)

  def buildOneForgeRateResponse(
      pair: Rate.Pair = Rate.Pair(from = EUR, to = USD),
      price: Price = Price(123.1234),
      timestamp: OffsetDateTime = testOffsetDateTime
  ): OneForgeRateResponse = OneForgeRateResponse(
    pair = pair,
    price = price,
    timestamp = Timestamp(timestamp)
  )

  def buildGetQuota(
      quotaUsed: Int = 10,
      quotaLimit: Int = 11,
      quotaRemaining: Int = 1,
      hoursUntilReset: Int = 1
  ): GetQuotaResponse =
    GetQuotaResponse(
      OneForgeQuotaResponse(
        remaining = quotaRemaining,
        hoursUntilReset = hoursUntilReset
      )
    )
}
