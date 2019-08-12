package forex.services.rates

import forex.config.ConfigFixtures.testApplicationConfig
import forex.domain.Currency.fromToPairs
import org.http4s.Method
import org.scalatest.{FreeSpec, Matchers}

class OneForgeApiSpec extends FreeSpec with Matchers {

  "buildOneForgeQuotesRequest" - {
    "should successfully decode GetRatesResponse containing OneForgeRatePair" in {
      val oneForgeConfig = testApplicationConfig.http.oneForge
      val actualRequest = OneForgeApi.buildOneForgeRequestQuotes(oneForgeConfig)
      actualRequest.uri.renderString should include("/quotes")
      actualRequest.method shouldBe Method.GET
      actualRequest.params("pairs") shouldBe fromToPairs.map(_.asString).mkString(",")
    }
  }
}
