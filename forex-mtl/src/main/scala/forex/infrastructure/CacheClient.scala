package forex.infrastructure

import cats.effect.Async
import cats.implicits._
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}
import scalacache._

import scala.concurrent.duration.Duration

class CacheClient[F[_]](implicit cache: Cache[Map[Json, Json]], M: Mode[F], F: Async[F]) {

  def getEntryValue[K: Encoder, V: Decoder](cacheKeyName: String)(entryKey: K): F[Option[V]] =
    for {
      maybeValue   <- F.map(get[F, Map[Json, Json]](cacheKeyName))(_.flatMap(_.get(entryKey.asJson)))
      decodedValue <- F.fromEither(maybeValue.traverse(_.as[V]))
    } yield decodedValue

  def putEntries[K: Encoder, V: Encoder](cacheKeyName: String, timeout: Option[Duration])(
      entries: Map[K, V]
  ): F[Done] =
    if (entries.isEmpty) F.pure(Done)
    else
      F.as(
        put[F, Map[Json, Json]](cacheKeyName)(entries.map { case (key, value) => (key.asJson, value.asJson) }, timeout),
        Done
      )
}

object CacheClient {

  def apply[F[_]: Async](underlyingCache: Cache[Map[Json, Json]]): CacheClient[F] = {
    implicit val mode: Mode[F]                 = scalacache.CatsEffect.modes.async[F]
    implicit val cache: Cache[Map[Json, Json]] = underlyingCache
    new CacheClient[F]
  }
}
