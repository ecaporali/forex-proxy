package forex.domain

import forex.domain.Currency._
import io.circe.Json.fromString
import io.circe.{DecodingFailure, Encoder, parser}
import org.scalatest.{FreeSpec, Matchers}

class CurrencySpec extends FreeSpec with Matchers {

  "Currency" - {

    "values" - {
      "should contain all the currencies" in {
        values shouldBe List(AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD)
      }
    }

    "fromToPairs" - {
      "should construct the product of all unique currency pair" in {
        fromToPairs.size shouldBe ((values.size * values.size) - values.size)
      }
    }

    "asString" - {
      "should convert a Currency into a raw value" in {
        Currency.values.foreach {
          case c@Currency.AUD => Currency.asString.show(c) shouldBe "AUD"
          case c@Currency.CAD => Currency.asString.show(c) shouldBe "CAD"
          case c@Currency.CHF => Currency.asString.show(c) shouldBe "CHF"
          case c@Currency.EUR => Currency.asString.show(c) shouldBe "EUR"
          case c@Currency.GBP => Currency.asString.show(c) shouldBe "GBP"
          case c@Currency.NZD => Currency.asString.show(c) shouldBe "NZD"
          case c@Currency.JPY => Currency.asString.show(c) shouldBe "JPY"
          case c@Currency.SGD => Currency.asString.show(c) shouldBe "SGD"
          case c@Currency.USD => Currency.asString.show(c) shouldBe "USD"
        }
      }
    }

    "decoder" - {
      "should successfully decode a raw value into a Currency" in {
        parser.decode[Currency]("\"AUD\"") shouldBe Right(Currency.AUD)
      }

      "should return an exception when raw value cannot be decoded" in {
        parser.decode[Currency]("\"WRONG\"") shouldBe Left(DecodingFailure("Unknown currency code WRONG", List.empty))
      }
    }

    "encoder" - {
      "should successfully encode a Currency into a json value" in {
        Encoder[Currency].apply(Currency.AUD) shouldBe fromString("AUD")
      }
    }
  }
}
