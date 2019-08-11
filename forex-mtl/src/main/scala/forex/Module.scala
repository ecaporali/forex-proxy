package forex

import cats.effect.{ Concurrent, Timer }
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.infrastructure.{ CacheClient, HttpClient }
import forex.programs._
import forex.services._
import io.chrisdavenport.log4cats.Logger
import io.circe.Json
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }
import scalacache.Cache

class Module[F[_]: Concurrent: Timer: Logger] private (
    config: ApplicationConfig,
    httpClient: HttpClient[F],
    cacheClient: CacheClient[F]
) {

  private val ratesService: RatesService[F] = RatesServices.ratesInterpreter[F](
    config = config.http,
    httpClient = httpClient
  )

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](
    config = config.http.rates,
    ratesService = ratesService,
    cacheClient = cacheClient
  )

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](
    handleGetRate = ratesProgram.get
  ).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.server.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}

object Module {
  def apply[F[_]: Concurrent: Timer](
      config: ApplicationConfig,
      httpBlazeClient: Client[F],
      scalaCache: Cache[Map[String, Json]],
      http4log: Logger[F]
  ): Module[F] = {
    implicit val logger: Logger[F] = http4log
    new Module[F](
      config = config,
      httpClient = new HttpClient[F](httpBlazeClient),
      cacheClient = CacheClient[F](scalaCache)
    )
  }
}
