package forex.domain

import java.time.OffsetDateTime

import forex.TestInstances.testOffsetDateTime
import forex.domain.Currency.{ AUD, JPY }

object RateFixtures {

  def buildRate(
      fromCurrency: Currency = AUD,
      toCurrency: Currency = JPY,
      price: Price = Price(100),
      offsetDateTime: OffsetDateTime = testOffsetDateTime
  ) = Rate(
    pair = Rate.Pair(from = fromCurrency, to = toCurrency),
    price = price,
    timestamp = Timestamp(offsetDateTime)
  )
}
