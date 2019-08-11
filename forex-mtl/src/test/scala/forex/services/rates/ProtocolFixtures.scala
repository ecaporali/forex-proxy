package forex.services.rates

import java.time.OffsetDateTime

import forex.TestInstances.testOffsetDateTime
import forex.domain.Currency.{EUR, USD}
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.Protocol.OneForgeProtocol.OneForgeRateResponse
import forex.services.rates.Protocol.{GetRatesResponse, RateResponse}

object ProtocolFixtures {

  def buildGetRatesResponse(
      ratesResponse: Vector[RateResponse] = Vector(buildOneForgeRateResponse())
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
}
