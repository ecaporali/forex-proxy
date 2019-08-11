package forex

import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}

object TestInstances {

  val testOffsetDateTime: OffsetDateTime = OffsetDateTime.of(
    LocalDateTime.of(2019, 8, 3, 0, 0, 0, 0),
    ZoneOffset.UTC
  )
}
