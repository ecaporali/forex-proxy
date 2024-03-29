package forex.domain

import cats.syntax.show._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

final case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  ) {
    lazy val asString: String = show"$from$to"
  }

  implicit val rateEncoder: Encoder[Rate] =
    deriveEncoder[Rate]

  implicit val pairEncoder: Encoder[Pair] =
    deriveEncoder[Pair]

  implicit val rateDecoder: Decoder[Rate] =
    deriveDecoder[Rate]

  implicit val pairDecoder: Decoder[Pair] =
    deriveDecoder[Pair]
}
