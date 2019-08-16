package forex.programs.rates

import cats.effect.IO
import forex.TestInstances.{noopLogger, testOffsetDateTime}
import forex.TestUtilsIO
import forex.domain.Currency.{AUD, JPY}
import forex.domain.{Price, Rate, RateFixtures, Timestamp}
import forex.infrastructure.Done
import forex.programs.rates.errors.Error.{CachedRateNotFound, RateLookupFailed, ServiceTemporaryUnavailable}
import forex.services.rates.errors.Error.{OneForgeLookupFailed, OneForgeQuotaLimitExceeded}
import org.scalatest.{FreeSpec, Matchers}

class ProgramSpec extends FreeSpec with Matchers with TestUtilsIO {

  "Program" - {

    val rate = RateFixtures.buildRate()

    "get rate" - {

      "should get cached rates when are found" in {
        val program: Program[IO] = new Program[IO](
          IO.pure(Right(Vector(RateFixtures.buildRate()))),
          _ => IO.pure(Some(rate)),
          _ => IO.pure(Done)
        )

        val expectedProgram = program.get(Protocol.GetRatesRequest(AUD, JPY))
        runIO(expectedProgram) shouldBe Right(rate)
      }

      "should get fresh rates when not cached in cache" in {
        val rate = Rate(Rate.Pair(from = AUD, to = JPY), Price(123.1234), Timestamp(testOffsetDateTime))

        val program: Program[IO] = new Program[IO](
          IO.pure(Right(Vector(rate))),
          _ => IO.pure(None),
          _ => IO.pure(Done)
        )

        val expectedProgram = program.get(Protocol.GetRatesRequest(AUD, JPY))
        runIO(expectedProgram) shouldBe Right(rate)
      }

      "should get a fixed rate when the currencies are the same" in {
        val program: Program[IO] = new Program[IO](
          IO.pure(Right(Vector(rate))),
          _ => IO.pure(None),
          _ => IO.pure(Done)
        )

        val expectedResultProgram = program.get(Protocol.GetRatesRequest(AUD, AUD))
        val expectedProgram       = runIO(expectedResultProgram)

        expectedProgram shouldBe 'right
        expectedProgram match {
          case Right(expectedRate) => expectedRate shouldBe Rate(Rate.Pair(AUD, AUD), Price(1), expectedRate.timestamp)
          case Left(_)             => fail("fail test - should not happen")
        }
      }

      "should return RateLookupFailed exception when it fails to get fresh rates" in {
        val program: Program[IO] = new Program[IO](
          IO.pure(Left(OneForgeLookupFailed("Failed to return rates"))),
          _ => IO.pure(None),
          _ => IO.pure(Done)
        )

        val expectedProgram = program.get(Protocol.GetRatesRequest(AUD, JPY))
        an [RateLookupFailed] should be thrownBy runIO(expectedProgram)
      }

      "should return ServiceTemporaryUnavailable exception when it exceeds the daily quota to fetch fresh rates" in {
        val program: Program[IO] = new Program[IO](
          IO.pure(Left(OneForgeQuotaLimitExceeded("Quota limit exceeded"))),
          _ => IO.pure(None),
          _ => IO.pure(Done)
        )

        val expectedProgram = program.get(Protocol.GetRatesRequest(AUD, JPY))
        an [ServiceTemporaryUnavailable] should be thrownBy runIO(expectedProgram)
      }

      "should return CachedRateNotFound exception when it fails to get fresh rates" in {
        val program: Program[IO] = new Program[IO](
          IO.pure(Right(Vector.empty)),
          _ => IO.pure(None),
          _ => IO.pure(Done)
        )

        val expectedProgram = program.get(Protocol.GetRatesRequest(AUD, JPY))
        runIO(expectedProgram) shouldBe Left(CachedRateNotFound("Requested rate cannot be found"))
      }
    }
  }
}
