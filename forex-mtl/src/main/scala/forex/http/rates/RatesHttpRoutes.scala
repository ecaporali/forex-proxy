package forex.http
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.domain.Rate
import forex.http.CustomHeaders.JsonContentTypeHeader
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.Error
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]](handleGetRate: GetRatesRequest => F[Error Either Rate])(implicit F: Sync[F])
  extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      handleGetRate(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap(F.fromEither).flatMap { rate =>
        Ok(rate.asGetApiResponse, JsonContentTypeHeader)
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
