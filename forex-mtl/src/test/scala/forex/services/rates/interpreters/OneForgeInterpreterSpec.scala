package forex.services.rates.interpreters

import cats.effect.IO
import forex.TestInstances.noopLogger
import forex.TestUtilsIO
import forex.config.ConfigFixtures.testApplicationConfig
import forex.services.rates.Algebra
import forex.services.rates.ProtocolFixtures.{ buildGetRatesResponse, buildOneForgeRateResponse }
import forex.services.rates.errors.Error.OneForgeLookupFailed
import org.scalatest.{ FreeSpec, Matchers }

class OneForgeInterpreterSpec extends FreeSpec with Matchers with TestUtilsIO {

  "OneForgeInterpreter" - {
    val successfulGetRatesResponse = buildGetRatesResponse(Vector(buildOneForgeRateResponse()))

    val interpreterSuccessful: Algebra[IO] = new OneForgeInterpreter[IO](
      oneForgeConfig = testApplicationConfig.http.oneForge,
      fetchRates = _ => IO(Right(successfulGetRatesResponse))
    )

    val interpreterFailure: Algebra[IO] = new OneForgeInterpreter[IO](
      oneForgeConfig = testApplicationConfig.http.oneForge,
      fetchRates = _ => IO(Left(new RuntimeException("Failed to retrieve rates")))
    )

    "getRates" - {
      "should successfully return GetRatesResponse when call succeeds" in {
        val ratesResult = interpreterSuccessful.getRates
        runIO(ratesResult) shouldBe Right(successfulGetRatesResponse)
      }

      "should successfully return OneForgeLookupFailed when call fails" in {
        val ratesResult = interpreterFailure.getRates
        runIO(ratesResult) shouldBe Left(OneForgeLookupFailed("Failed to retrieve rates"))
      }
    }
  }
}
