package forex.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}

case class Price(value: BigDecimal) extends AnyVal

object Price {
  def apply(value: Int): Price =
    Price(BigDecimal(value))

  implicit val priceEncoder: Encoder[Price] =
    deriveUnwrappedEncoder[Price]

  implicit val priceDecoder: Decoder[Price] =
    deriveUnwrappedDecoder[Price]
}
