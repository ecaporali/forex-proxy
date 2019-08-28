package forex.services.rates

import forex.config.ConfigFixtures.testApplicationConfig
import forex.domain.Currency.uniqueProductPairs
import org.http4s.Method
import org.scalatest.{FreeSpec, Matchers}

class OneForgeApiSpec extends FreeSpec with Matchers {

  "buildOneForgeRequestQuotes" - {
    "should successfully build request containing /quotes endpoint" in {
      val oneForgeConfig = testApplicationConfig.http.oneForge
      val actualRequest  = OneForgeApi.buildOneForgeRequestQuotes(oneForgeConfig)
      actualRequest.uri.renderString should include("/quotes")
      actualRequest.method shouldBe Method.GET
      actualRequest.params("pairs") shouldBe uniqueProductPairs.map(_.asString).mkString(",")
    }
  }

  "buildOneForgeRequestQuota" - {
    "should successfully build request containing /quota endpoint" in {
      val oneForgeConfig = testApplicationConfig.http.oneForge
      val actualRequest  = OneForgeApi.buildOneForgeRequestQuota(oneForgeConfig)
      actualRequest.uri.renderString should include("/quota")
      actualRequest.method shouldBe Method.GET
    }
  }
}
