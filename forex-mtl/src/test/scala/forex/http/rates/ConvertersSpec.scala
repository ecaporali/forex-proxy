package forex.http.rates

import java.time.OffsetDateTime

import forex.domain.Currency.{AUD, JPY}
import forex.domain.{Price, RateFixtures, Timestamp}
import forex.http.rates.Converters._
import forex.http.rates.Protocol.GetApiResponse
import org.scalatest.{FlatSpec, Matchers}

class ConvertersSpec extends FlatSpec with Matchers {

  "Rate converters asGetApiResponse" should "successfully convert a rate into GetApiResponse" in {
    val now  = OffsetDateTime.now
    val rate = RateFixtures.buildRate(AUD, JPY, Price(100), now)

    rate.asGetApiResponse shouldBe GetApiResponse(
      from = AUD,
      to = JPY,
      price = Price(100),
      timestamp = Timestamp(now)
    )
  }
}
