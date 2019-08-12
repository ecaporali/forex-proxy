package forex.services.rates

import cats.syntax.functor._
import forex.domain.{Currency, Price, Rate, Timestamp}
import io.circe.Json.fromString
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder
import io.circe.{Decoder, HCursor}

object Protocol {

  case class GetRatesResponse(
      rates: Seq[RateResponse]
  )

  sealed trait RateResponse {
    val pair: Rate.Pair
    val price: Price
    val timestamp: Timestamp
  }

  lazy implicit val getRatesResponseDecoder: Decoder[GetRatesResponse] =
    deriveUnwrappedDecoder

  lazy implicit val rateResponseDecoder: Decoder[RateResponse] =
    List[Decoder[RateResponse]](
      OneForgeProtocol.decodeOneForgeRateResponse.widen,
    ).reduceLeft(_ or _)

  object OneForgeProtocol {

    case class GetQuotaResponse(
        quotaUsed: Int,
        quotaLimit: Int,
        quotaRemaining: Int,
        hoursUntilReset: Int
    )

    final case class RateResponse(
        pair: Rate.Pair,
        price: Price,
        timestamp: Timestamp
    ) extends Protocol.RateResponse

    implicit def decodeOneForgeQuota: Decoder[GetQuotaResponse] =
      (cursor: HCursor) =>
        for {
          quotaUsed <- cursor.downField("quota_used").as[Int]
          quotaLimit <- cursor.downField("quota_limit").as[Int]
          quotaRemaining <- cursor.downField("quota_remaining").as[Int]
          hoursUntilReset <- cursor.downField("hours_until_reset").as[Int]
        } yield GetQuotaResponse(quotaUsed, quotaLimit, quotaRemaining, hoursUntilReset)

    private[rates] def decodeOneForgeRateResponse: Decoder[OneForgeProtocol.RateResponse] =
      (cursor: HCursor) =>
        for {
          rawSymbol <- cursor.downField("symbol").as[String]
          from <- fromString(rawSymbol.take(3)).as[Currency]
          to <- fromString(rawSymbol.drop(3)).as[Currency]
          price <- cursor.downField("price").as[Price]
          timestamp <- cursor.downField("timestamp").as[Timestamp]
        } yield RateResponse(Rate.Pair(from, to), price, timestamp)
  }
}
