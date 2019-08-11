package forex.services.rates

import cats.effect.Sync
import forex.config.HttpConfig
import forex.domain.Rate
import forex.infrastructure.HttpClient
import forex.services.rates.interpreters._
import io.circe.generic.auto._

object Interpreters {
  def ratesInterpreter[F[_]: Sync](config: HttpConfig, httpClient: HttpClient[F]): Algebra[F] =
    new OneForgeInterpreter[F](
      config = config.oneForge,
      fetchRates = httpClient.executeRequest[Rate]
    )
}
