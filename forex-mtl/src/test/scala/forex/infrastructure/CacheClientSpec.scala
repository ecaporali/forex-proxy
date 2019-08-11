package forex.infrastructure

import cats.effect.IO
import forex.TestInstances.testCaffeineCache
import forex.TestUtilsIO
import io.circe.Json
import org.scalatest.{ FreeSpec, Matchers }

import scala.concurrent.duration._

class CacheClientSpec extends FreeSpec with Matchers with TestUtilsIO {

  val cacheClient: CacheClient[IO] = CacheClient[IO](testCaffeineCache)

  "CacheClient" - {

    "getEntryValue without timout" - {
      val cacheKey = "no-timeout-test-key"
      runIO(cacheClient.putEntries(cacheKey, None)(Map("key1" -> "value1", "key2" -> "value2")))

      "should successfully retrieve entry value" in {
        val expectedValue = cacheClient.getEntryValue[Json](cacheKey)("key2")
        runIO(expectedValue) shouldBe Some(Json.fromString("value2"))
      }

      "should return NONE when entry is not found" in {
        val expectedValue = cacheClient.getEntryValue[Json](cacheKey)("MISSING-ENTRY-KEY")
        runIO(expectedValue) shouldBe None
      }

      "should return NONE when current cache is not found" in {
        val expectedValue = cacheClient.getEntryValue[Json]("MISSING-CACHE-KEY")("key2")
        runIO(expectedValue) shouldBe None
      }
    }

    "getEntryValue with timout" - {
      val cacheKey = "with-timeout-test-key"
      runIO(cacheClient.putEntries(cacheKey, Some(1.nano))(Map("key" -> "value")))

      "should return NONE when timeout is expired" in {
        val expectedValue = cacheClient.getEntryValue[Json](cacheKey)("key")
        runIO(expectedValue) shouldBe None
      }
    }

    "setEntries" - {
      "should successfully store entries" in {
        val expectedResult = cacheClient.putEntries[String]("test-key", None)(Map("key1" -> "value1"))
        runIO(expectedResult) shouldBe Done
      }

      "should short-circuit store operation when entries are empty" in {
        val expectedResult = cacheClient.putEntries[String]("empty-map-key", None)(Map.empty)
        runIO(expectedResult) shouldBe Done
      }
    }
  }
}
