package forex.domain

import java.time.Instant.ofEpochSecond
import java.time.OffsetDateTime.ofInstant
import java.time.temporal.ChronoUnit
import java.time.{OffsetDateTime, ZoneOffset}

import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}
import io.circe.{Decoder, Encoder}

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS))

  implicit val timestampEncoder: Encoder[Timestamp] =
    deriveUnwrappedEncoder

  implicit val timestampDecoder: Decoder[Timestamp] =
    List[Decoder[Timestamp]](
      Decoder.decodeLong.map(epochSecond => Timestamp(ofInstant(ofEpochSecond(epochSecond), ZoneOffset.UTC))),
      deriveUnwrappedDecoder
    ).reduceLeft(_ or _)
}
