package forex.services.rates

import forex.config.OneForgeConfig
import forex.domain.Currency
import forex.infrastructure.HttpClient.createGetRequest
import org.http4s.Request

object OneForgeApi {

  def buildOneForgeRequestQuotes[F[_]](oneForgeConfig: OneForgeConfig): Request[F] = {
    val pairs = Currency.uniqueProductPairs.map(_.asString).mkString(",")
    createGetRequest[F](
      uri = (oneForgeConfig.uri / "quotes")
        .withQueryParam("pairs", pairs)
        .withQueryParam("api_key", oneForgeConfig.apiKey)
    )
  }

  def buildOneForgeRequestQuota[F[_]](oneForgeConfig: OneForgeConfig): Request[F] = {
    createGetRequest[F](
      uri = (oneForgeConfig.uri / "quota")
        .withQueryParam("api_key", oneForgeConfig.apiKey)
    )
  }
}
