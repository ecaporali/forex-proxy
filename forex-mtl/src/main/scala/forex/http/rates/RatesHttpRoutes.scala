package forex.http
package rates

import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.domain.Rate
import forex.http.CustomHeaders.JsonContentTypeHeader
import forex.http.rates.Converters._
import forex.http.rates.Protocol._
import forex.http.rates.QueryParams._
import forex.programs.ProgramErrorOr
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.Error.{ CachedRateNotFound, RateLookupFailed, UnknownCurrency }
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import io.chrisdavenport.log4cats.Logger
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{ HttpRoutes, Response }

class RatesHttpRoutes[F[_]: Logger](
    handleGetRate: GetRatesRequest => F[ProgramErrorOr[Rate]]
)(implicit F: Sync[F])
    extends Http4sDsl[F] {

  private[http] val prefixPath = "/v1/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(fromOrError) +& ToQueryParam(toOrError) => {
      val rateOrError = for {
        from <- F.fromEither(fromOrError)
        to <- F.fromEither(toOrError)
        rateOrError <- handleGetRate(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap(F.fromEither)
      } yield rateOrError

      rateOrError
        .flatMap(rate => Ok(rate.asGetApiResponse))
        .recoverWith {
          case UnknownCurrency(msg)     => BadRequest(ApiErrorResponse(msg))
          case CachedRateNotFound(msg)  => NotFound(ApiErrorResponse(msg))
          case RateLookupFailed(msg)    => BadGateway(ApiErrorResponse(msg))
          case uncaughtError: Throwable => logAndReturnInternalError(uncaughtError)
        }
        .map(_.withHeaders(JsonContentTypeHeader))
    }
  }

  private def logAndReturnInternalError(uncaughtError: Throwable): F[Response[F]] =
    for {
      _ <- Logger[F].error(uncaughtError)("Something went wrong. Please try again or check the logs.")
      internalServerError <- InternalServerError(ApiErrorResponse(uncaughtError.getMessage))
    } yield internalServerError

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
