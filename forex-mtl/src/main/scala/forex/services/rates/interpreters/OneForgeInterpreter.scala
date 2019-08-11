package forex.services.rates.interpreters

import cats.effect.Sync
import cats.implicits._
import forex.config.OneForgeConfig
import forex.domain.{ Price, Rate, Timestamp }
import forex.infrastructure.ErrorOr
import forex.services.ServiceErrorOr
import forex.services.rates.Protocol.GetRatesResponse
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.OneForgeLookupFailed
import forex.services.rates.{ Algebra, OneForgeApi }
import io.chrisdavenport.log4cats.Logger
import org.http4s.Request

class OneForgeInterpreter[F[_]: Sync: Logger](
    oneForgeConfig: OneForgeConfig,
    fetchRates: Request[F] => F[ErrorOr[GetRatesResponse]]
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[ServiceErrorOr[Rate]] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]

  override def getRates: F[ServiceErrorOr[GetRatesResponse]] = {
    val oneForgeQuotesRequest = OneForgeApi.buildOneForgeQuotesRequest[F](oneForgeConfig)

    for {
      _ <- Logger[F].info(s"Fetching rates from OneForge: ${oneForgeQuotesRequest.uri.path}")
      errorOrRatesResponse <- fetchRates(oneForgeQuotesRequest)
      oneForgeErrorOrRates <- logAndTransformError(errorOrRatesResponse)
    } yield oneForgeErrorOrRates
  }

  private def logAndTransformError(
      errorOrRates: ErrorOr[GetRatesResponse]
  ): F[ServiceErrorOr[GetRatesResponse]] =
    errorOrRates.leftTraverse(
      error =>
        for {
          _ <- Logger[F].error(error)("Error fetching rates from OneForge")
        } yield OneForgeLookupFailed(error.getMessage)
    )
}
