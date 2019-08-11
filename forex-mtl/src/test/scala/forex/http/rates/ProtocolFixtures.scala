package forex.http.rates

import java.time.OffsetDateTime

import forex.TestInstances.testOffsetDateTime
import forex.domain.Currency.{AUD, JPY}
import forex.domain.{Currency, Price, Timestamp}
import forex.http.rates.Protocol.GetApiResponse

object ProtocolFixtures {

  def buildGetApiResponse(
      from: Currency = AUD,
      to: Currency = JPY,
      price: Price = Price(100),
      offsetDateTime: OffsetDateTime = testOffsetDateTime
  ) = GetApiResponse(
    from = AUD,
    to = JPY,
    price = Price(100),
    timestamp = Timestamp(offsetDateTime)
  )
}
