package forex.services.rates

import cats.syntax.functor._
import forex.domain.{Currency, Price, Rate, Timestamp}
import io.circe.Json.fromString
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder
import io.circe.{Decoder, HCursor}

object Protocol {

  case class GetRatesResponse(rates: List[RateResponse])
  case class GetQuotaResponse(quota: QuotaResponse)

  sealed trait RateResponse {
    def pair: Rate.Pair
    def price: Price
    def timestamp: Timestamp
  }

  sealed trait QuotaResponse {
    def remaining: Int
    def hoursUntilReset: Int
  }

  object GetRatesResponse {

    final case class OneForgeRateResponse(pair: Rate.Pair, price: Price, timestamp: Timestamp) extends RateResponse

    private[rates] val oneForgeRateResponseDecoder: Decoder[RateResponse] = (cursor: HCursor) =>
      for {
        rawSymbol <- cursor.downField("symbol").as[String]
        from      <- fromString(rawSymbol.take(3)).as[Currency]
        to        <- fromString(rawSymbol.drop(3)).as[Currency]
        price     <- cursor.downField("price").as[Price]
        timestamp <- cursor.downField("timestamp").as[Timestamp]
      } yield OneForgeRateResponse(Rate.Pair(from, to), price, timestamp)
  }

  object GetQuotaResponse {

    final case class OneForgeQuotaResponse(remaining: Int, hoursUntilReset: Int) extends QuotaResponse

    private[rates] val oneForgeQuotaResponseDecoder: Decoder[OneForgeQuotaResponse] = (cursor: HCursor) =>
      for {
        quotaRemaining  <- cursor.downField("quota_remaining").as[Int]
        hoursUntilReset <- cursor.downField("hours_until_reset").as[Int]
      } yield OneForgeQuotaResponse(quotaRemaining, hoursUntilReset)
  }

  lazy implicit val rateResponseDecoder: Decoder[RateResponse] = {
    GetRatesResponse.oneForgeRateResponseDecoder.widen
  }

  lazy implicit val quotaResponseDecoder: Decoder[QuotaResponse] = {
    GetQuotaResponse.oneForgeQuotaResponseDecoder.widen
  }

  lazy implicit val getRatesResponseDecoder: Decoder[GetRatesResponse] =
    deriveUnwrappedDecoder

  lazy implicit val getQuotaResponseDecoder: Decoder[GetQuotaResponse] =
    deriveUnwrappedDecoder
}
