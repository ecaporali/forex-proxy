package forex.services.rates

import cats.syntax.functor._
import forex.domain.{Currency, Price, Rate, Timestamp}
import io.circe.Decoder
import io.circe.Json.fromString
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder

object Protocol {

  case class GetRatesResponse(
      rates: Seq[RateResponse]
  )

  sealed trait RateResponse {
    val pair: Rate.Pair
    val price: Price
    val timestamp: Timestamp
  }

  object OneForgeProtocol {

    final case class OneForgeRateResponse(
        pair: Rate.Pair,
        price: Price,
        timestamp: Timestamp
    ) extends RateResponse

    private[rates] def decodeOneForgeRate: Decoder[OneForgeRateResponse] =
      Decoder.instance { cursor =>
        for {
          rawSymbol <- cursor.downField("symbol").as[String]
          from <- fromString(rawSymbol.take(3)).as[Currency]
          to <- fromString(rawSymbol.drop(3)).as[Currency]
          price <- cursor.downField("price").as[Price]
          timestamp <- cursor.downField("timestamp").as[Timestamp]
        } yield OneForgeRateResponse(Rate.Pair(from, to), price, timestamp)
      }
  }

  lazy implicit val getRatesResponseDecoder: Decoder[GetRatesResponse] =
    deriveUnwrappedDecoder

  lazy implicit val rateResponseDecoder: Decoder[RateResponse] =
    List[Decoder[RateResponse]](
      OneForgeProtocol.decodeOneForgeRate.widen,
    ).reduceLeft(_ or _)
}
