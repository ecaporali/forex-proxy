package forex.services.rates

import cats.effect.Sync
import forex.config.HttpConfig
import forex.infrastructure.HttpClient
import forex.services.rates.Protocol.{GetQuotaResponse, GetRatesResponse}
import forex.services.rates.interpreters._
import io.chrisdavenport.log4cats.Logger

object Interpreters {
  def ratesInterpreter[F[_]: Sync: Logger](config: HttpConfig, httpClient: HttpClient[F]): Algebra[F] =
    new OneForgeInterpreter[F](
      oneForgeConfig = config.oneForge,
      fetchRates = httpClient.executeRequest[GetRatesResponse],
      fetchQuota = httpClient.executeRequest[GetQuotaResponse]
    )
}
