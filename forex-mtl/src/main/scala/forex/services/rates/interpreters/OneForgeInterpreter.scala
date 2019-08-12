package forex.services.rates.interpreters

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import forex.config.OneForgeConfig
import forex.domain.Rate
import forex.infrastructure.ErrorOr
import forex.services.ServiceErrorOr
import forex.services.rates.Protocol.GetRatesResponse
import forex.services.rates.Protocol.OneForgeProtocol.GetQuotaResponse
import forex.services.rates.errors.Error.{OneForgeLookupFailed, OneForgeQuotaLimitExceeded}
import forex.services.rates.{Algebra, OneForgeApi}
import io.chrisdavenport.log4cats.Logger
import org.http4s.Request
import forex.services.rates.Converters._

class OneForgeInterpreter[F[_]: Logger](
    oneForgeConfig: OneForgeConfig,
    fetchRates: Request[F] => F[ErrorOr[GetRatesResponse]],
    fetchQuota: Request[F] => F[ErrorOr[GetQuotaResponse]]
)(implicit F: Sync[F])
    extends Algebra[F] {

  override def getRates: F[ServiceErrorOr[Seq[Rate]]] =
    (for {
      _ <- EitherT(ensureAvailableQuotaOrError)
      freshRatesResponse <- EitherT(executeFetchRates)
    } yield freshRatesResponse.toRates).value

  private def ensureAvailableQuotaOrError: F[ServiceErrorOr[GetQuotaResponse]] = {
    val oneForgeRequestQuota = OneForgeApi.buildOneForgeRequestQuota[F](oneForgeConfig)
    for {
      _ <- Logger[F].info(s"Fetching current quota from OneForge: ${oneForgeRequestQuota.uri.path}")
      quotaResponse <- fetchQuota(oneForgeRequestQuota).flatMap(F.fromEither)
    } yield verifyQuotaLimit(quotaResponse)
  }

  private def executeFetchRates: F[ServiceErrorOr[GetRatesResponse]] = {
    val oneForgeRequestQuotes = OneForgeApi.buildOneForgeRequestQuotes[F](oneForgeConfig)
    for {
      _ <- Logger[F].info(s"Fetching fresh rates from OneForge: ${oneForgeRequestQuotes.uri.path}")
      errorOrRatesResponse <- fetchRates(oneForgeRequestQuotes).flatMap(logAndTransformError)
    } yield errorOrRatesResponse
  }

  private def verifyQuotaLimit(quotaResponse: GetQuotaResponse): ServiceErrorOr[GetQuotaResponse] =
    if (quotaResponse.quotaRemaining > 0) Either.right(quotaResponse)
    else Either.left(OneForgeQuotaLimitExceeded(s"Maximum daily quota reached, please try again in ${quotaResponse.hoursUntilReset} hour(s)"))

  private def logAndTransformError(
      errorOrResponse: ErrorOr[GetRatesResponse]
  ): F[ServiceErrorOr[GetRatesResponse]] =
    errorOrResponse.leftTraverse(
      error =>
        for {
          _ <- Logger[F].error(error)("Error fetching rates from OneForge")
        } yield OneForgeLookupFailed(error.getMessage)
    )
}
