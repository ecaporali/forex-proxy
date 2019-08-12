package forex.services.rates.interpreters

import cats.effect.IO
import forex.TestInstances.noopLogger
import forex.TestUtilsIO
import forex.config.ConfigFixtures.testApplicationConfig
import forex.domain.Rate
import forex.services.rates.Algebra
import forex.services.rates.ProtocolFixtures.{buildGetQuota, buildGetRatesResponse, buildOneForgeRateResponse}
import forex.services.rates.errors.Error.{OneForgeLookupFailed, OneForgeQuotaLimitExceeded}
import org.scalatest.{FreeSpec, Matchers}

class OneForgeInterpreterSpec extends FreeSpec with Matchers with TestUtilsIO {

  "OneForgeInterpreter" - {
    val successfulGetRatesResponse = buildGetRatesResponse(Vector(buildOneForgeRateResponse()))

    val interpreterSuccessful: Algebra[IO] = new OneForgeInterpreter[IO](
      oneForgeConfig = testApplicationConfig.http.oneForge,
      fetchRates = _ => IO(Right(successfulGetRatesResponse)),
      fetchQuota = _ => IO(Right(buildGetQuota(quotaRemaining = 10)))
    )

    val interpreterFailure: Algebra[IO] = new OneForgeInterpreter[IO](
      oneForgeConfig = testApplicationConfig.http.oneForge,
      fetchRates = _ => IO(Left(new RuntimeException("Failed to retrieve rates"))),
      fetchQuota = _ => IO(Right(buildGetQuota()))
    )

    val interpreterLimitExceeded: Algebra[IO] = new OneForgeInterpreter[IO](
      oneForgeConfig = testApplicationConfig.http.oneForge,
      fetchRates = _ => IO(Right(successfulGetRatesResponse)),
      fetchQuota = _ => IO(Right(buildGetQuota(quotaRemaining = 0, hoursUntilReset = 2)))
    )

    "getRates" - {
      "should successfully call OneForge api when quota is above limit" in {
        val ratesResult  = interpreterSuccessful.getRates
        val expectedRate = successfulGetRatesResponse.rates.head
        runIO(ratesResult) shouldBe Right(Vector(Rate(expectedRate.pair, expectedRate.price, expectedRate.timestamp)))
      }

      "should return OneForgeLookupFailed and NOT call OneForge api when quota is equal to limit" in {
        val ratesResult = interpreterLimitExceeded.getRates
        runIO(ratesResult) shouldBe Left(
          OneForgeQuotaLimitExceeded("Maximum daily quota reached, please try again in 2 hour(s)")
        )
      }

      "should return OneForgeLookupFailed when call fails" in {
        val ratesResult = interpreterFailure.getRates
        runIO(ratesResult) shouldBe Left(OneForgeLookupFailed("Failed to retrieve rates"))
      }
    }
  }
}
