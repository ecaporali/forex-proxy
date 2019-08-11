package forex.http.rates

import cats.syntax.either._
import forex.domain.Currency
import forex.programs.rates.errors.Error.UnknownCurrency
import io.circe.Json.fromString
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Exception Either Currency] =
    QueryParamDecoder[String].map(fromString(_).as[Currency].leftMap(e => UnknownCurrency(e.getMessage)))

  object FromQueryParam extends QueryParamDecoderMatcher[Exception Either Currency]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Exception Either Currency]("to")
}
