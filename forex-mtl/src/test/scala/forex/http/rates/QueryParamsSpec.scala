package forex.http.rates

import cats.data.Validated.Valid
import forex.domain.Currency.AUD
import forex.programs.rates.errors.Error.UnknownCurrency
import org.http4s.QueryParameterValue
import org.scalatest.{FreeSpec, Matchers}

class QueryParamsSpec extends FreeSpec with Matchers {

  "currencyQueryParam" - {
    "should successfully decode a supported Currency" in {
      val expectedCurrency = QueryParams.currencyQueryParam.decode(QueryParameterValue("AUD"))
      expectedCurrency shouldBe Valid(Right(AUD))
    }

    "should return UnknownCurrency when Currency is wrong or not supported" in {
      val expectedCurrency = QueryParams.currencyQueryParam.decode(QueryParameterValue("WRONG"))
      expectedCurrency shouldBe Valid(Left(UnknownCurrency("Unknown currency code WRONG")))
    }
  }
}
