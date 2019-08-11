package forex.domain

import cats.Show
import cats.kernel.Eq
import enumeratum._
import io.circe._

import scala.collection.immutable

sealed trait Currency extends EnumEntry

object Currency extends Enum[Currency] {

  case object AUD extends Currency
  case object CAD extends Currency
  case object CHF extends Currency
  case object EUR extends Currency
  case object GBP extends Currency
  case object NZD extends Currency
  case object JPY extends Currency
  case object SGD extends Currency
  case object USD extends Currency

  val values: immutable.IndexedSeq[Currency] = findValues

  val fromToPairs: Seq[Rate.Pair] = for {
    from <- values
    to <- values if from != to
  } yield Rate.Pair(from, to)

  implicit val asString: Show[Currency] = Show.show {
    case AUD => "AUD"
    case CAD => "CAD"
    case CHF => "CHF"
    case EUR => "EUR"
    case GBP => "GBP"
    case NZD => "NZD"
    case JPY => "JPY"
    case SGD => "SGD"
    case USD => "USD"
  }

  implicit val currencyEq: Eq[Currency] = Eq.fromUniversalEquals[Currency]

  implicit val currencyDecoder: Decoder[Currency] =
    Decoder.decodeString.emap(str => Currency.withNameInsensitiveOption(str).toRight(s"Unknown currency code $str"))

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { asString.show _ andThen Json.fromString }
}
