package forex

import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}

import cats.effect.{Clock, IO}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.noop.NoOpLogger
import io.circe.Json
import scalacache.Cache
import scalacache.caffeine.CaffeineCache

import scala.concurrent.duration.{MILLISECONDS, NANOSECONDS, TimeUnit}

object TestInstances {

  implicit val noopLogger: Logger[IO] = NoOpLogger.impl[IO]

  val testCaffeineCache: Cache[Map[Json, Json]] = CaffeineCache[Map[Json, Json]]

  val testOffsetDateTime: OffsetDateTime = OffsetDateTime.of(
    LocalDateTime.of(2019, 8, 3, 0, 0, 0, 0),
    ZoneOffset.UTC
  )

  class IOClock extends Clock[IO] {
    final def realTime(unit: TimeUnit): IO[Long] =
      IO(unit.convert(System.currentTimeMillis(), MILLISECONDS))
    final def monotonic(unit: TimeUnit): IO[Long] =
      IO(unit.convert(System.nanoTime(), NANOSECONDS))
  }
}
