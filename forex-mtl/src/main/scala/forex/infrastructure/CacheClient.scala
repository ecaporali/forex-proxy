package forex.infrastructure

import cats.effect.Async
import cats.implicits._
import io.circe.syntax.EncoderOps
import io.circe.{ Decoder, Encoder, Json }
import scalacache._
import scalacache.caffeine.CaffeineCache

import scala.concurrent.duration.Duration

class CacheClient[F[_]](implicit cache: Cache[Map[String, Json]], M: Mode[F], F: Async[F]) {

  def getEntryValue[A: Decoder](cacheKeyName: String)(entryKey: String): F[Option[A]] =
    for {
      maybeValue <- F.map(get[F, Map[String, Json]](cacheKeyName))(_.flatMap(_.get(entryKey)))
      decodedValueOrError = maybeValue.traverse(_.as[A])
      decodedValue <- F.fromEither(decodedValueOrError)
    } yield decodedValue

  def putEntries[A: Encoder](cacheKeyName: String, timeout: Option[Duration])(
    entries: Map[String, A]
  ): F[Done] =
    if (entries.isEmpty) F.pure(Done)
    else F.as(put[F, Map[String, Json]](cacheKeyName)(entries.mapValues(_.asJson), timeout), Done)
}

object CacheClient {

  def apply[F[_]: Async]: CacheClient[F] = {
    implicit val mode: Mode[F]                             = scalacache.CatsEffect.modes.async[F]
    implicit val underlyingCache: Cache[Map[String, Json]] = CaffeineCache[Map[String, Json]]
    new CacheClient[F]
  }
}
