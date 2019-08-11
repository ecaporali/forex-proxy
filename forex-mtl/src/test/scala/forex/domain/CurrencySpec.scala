package forex.domain

import forex.domain.Currency._
import io.circe.Json.fromString
import io.circe.{ parser, DecodingFailure, Encoder }
import org.scalatest.{ FreeSpec, Matchers }

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
