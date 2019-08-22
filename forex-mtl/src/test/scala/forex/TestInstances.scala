package forex

import java.time.{ LocalDateTime, OffsetDateTime, ZoneOffset }

import cats.effect.IO
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.noop.NoOpLogger
import io.circe.Json
import scalacache.Cache
import scalacache.caffeine.CaffeineCache

object TestInstances {

  implicit val noopLogger: Logger[IO] = NoOpLogger.impl[IO]

  val testCaffeineCache: Cache[Map[Json, Json]] = CaffeineCache[Map[Json, Json]]

  val testOffsetDateTime: OffsetDateTime = OffsetDateTime.of(
    LocalDateTime.of(2019, 8, 3, 0, 0, 0, 0),
    ZoneOffset.UTC
  )
}
