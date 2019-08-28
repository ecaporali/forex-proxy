package forex.domain

import java.time.Instant.ofEpochSecond
import java.time.OffsetDateTime.ofInstant
import java.time.{OffsetDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

import cats.Functor
import cats.effect.Clock
import cats.syntax.functor._
import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}
import io.circe.{Decoder, Encoder}

final case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now[F[_]: Clock: Functor]: F[Timestamp] =
    Clock[F].realTime(TimeUnit.SECONDS).map(toTimestamp)

  implicit val timestampEncoder: Encoder[Timestamp] =
    deriveUnwrappedEncoder

  implicit val timestampDecoder: Decoder[Timestamp] =
    List[Decoder[Timestamp]](
      Decoder.decodeLong.map(toTimestamp),
      deriveUnwrappedDecoder
    ).reduceLeft(_ or _)

  private def toTimestamp(epochSecond: Long): Timestamp =
    Timestamp(ofInstant(ofEpochSecond(epochSecond), ZoneOffset.UTC))
}
