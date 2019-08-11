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
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.Error
import forex.programs.rates.errors.Error.{ RateLookupFailed, UnknownCurrency }
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]](handleGetRate: GetRatesRequest => F[Error Either Rate])(implicit F: Sync[F])
    extends Http4sDsl[F] {

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(fromOrError) +& ToQueryParam(toOrError) =>
      val responseOrError = for {
        from <- F.fromEither(fromOrError)
        to <- F.fromEither(toOrError)
        responseOrError <- handleGetRate(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap(F.fromEither)
      } yield responseOrError

      responseOrError
        .flatMap(rate => Ok(rate.asGetApiResponse, JsonContentTypeHeader))
        .recoverWith {
          case error: UnknownCurrency  => BadRequest(error.msg, JsonContentTypeHeader)
          case error: RateLookupFailed => InternalServerError(error.msg, JsonContentTypeHeader)
          case otherError: Throwable   => InternalServerError(otherError.getMessage, JsonContentTypeHeader)
        }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
