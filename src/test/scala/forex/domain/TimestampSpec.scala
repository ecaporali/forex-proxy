package forex.domain

import java.time.format.DateTimeFormatter

import forex.TestInstances.testOffsetDateTime
import io.circe.parser
import org.scalatest.{ FreeSpec, Matchers }

class TimestampSpec extends FreeSpec with Matchers {

  "Timestamp" - {

    "decoder" - {
      "should successfully decode a raw OffsetDateTime value into a Timestamp" in {
        val offsetDateTime = DateTimeFormatter.ISO_INSTANT.format(testOffsetDateTime)
        parser.decode[Timestamp](s""""$offsetDateTime"""") shouldBe Right(Timestamp(testOffsetDateTime))
      }

      "should successfully decode a raw Instant into a Timestamp" in {
        parser.decode[Timestamp](s""""1564790400"""") shouldBe Right(Timestamp(testOffsetDateTime))
      }

      "should return an exception when raw value cannot be decoded" in {
        parser.decode[Timestamp](s""""BAD_VALUE"""") shouldBe 'left
      }
    }
  }
}
