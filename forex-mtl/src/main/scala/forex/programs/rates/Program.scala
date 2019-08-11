package forex.programs.rates

import cats.effect.Sync
import cats.syntax.either._
import forex.config.RatesConfig
import forex.domain._
import forex.infrastructure.{ CacheClient, Done }
import forex.programs.ProgramErrorOr
import forex.programs.rates.errors.Error.CachedRateNotFound
import forex.programs.rates.errors.toProgramError
import forex.services.rates.Protocol.GetRatesResponse
import forex.services.{ RatesService, ServiceErrorOr }
import io.chrisdavenport.log4cats.Logger

class Program[F[_]: Logger] private (
    getFreshRates: => F[ServiceErrorOr[GetRatesResponse]],
    getCachedRate: String => F[Option[Rate]],
    setCachedRates: Map[String, Rate] => F[Done]
)(implicit F: Sync[F])
    extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[ProgramErrorOr[Rate]] =
    F.map(getFreshRates)(_.leftMap(toProgramError).flatMap(getResponseRates => {
      val rates = getResponseRates.rates.map(r => Rate(r.pair, r.price, r.timestamp))
      rates
        .find(_.pair.asString == Rate.Pair(request.from, request.to).asString)
        .toRight(CachedRateNotFound("Requested rate cannot be found"))
    }))
}

object Program {

  def apply[F[_]: Sync: Logger](
      config: RatesConfig,
      ratesService: RatesService[F],
      cacheClient: CacheClient[F]
  ): Algebra[F] = new Program[F](
    getFreshRates = ratesService.getRates,
    getCachedRate = cacheClient.getEntryValue[Rate](
      cacheKeyName = config.cacheKeyName
    ),
    setCachedRates = cacheClient.putEntries[Rate](
      cacheKeyName = config.cacheKeyName,
      timeout = Some(config.priceTimeout)
    )
  )
}
