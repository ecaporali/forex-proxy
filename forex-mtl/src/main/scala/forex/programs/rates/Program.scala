package forex.programs.rates

import cats.effect.Sync
import cats.syntax.either._
import cats.syntax.eq._
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.config.RatesConfig
import forex.domain._
import forex.infrastructure.{CacheClient, Done}
import forex.programs.ProgramErrorOr
import forex.programs.rates.Converters._
import forex.programs.rates.errors.Error.CachedRateNotFound
import forex.programs.rates.errors.{Error, toProgramError}
import forex.services.{RatesService, ServiceErrorOr}
import io.chrisdavenport.log4cats.Logger

class Program[F[_]: Logger] private[rates] (
    getFreshRates: => F[ServiceErrorOr[Seq[Rate]]],
    getCachedRate: String => F[Option[Rate]],
    setCachedRates: Map[String, Rate] => F[Done]
)(implicit F: Sync[F])
    extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[ProgramErrorOr[Rate]] =
    if (request.from === request.to) F.pure(getFlatRate(request).asRight)
    else executeGetRequest(request.asPair)

  private def executeGetRequest(requestPair: Rate.Pair): F[ProgramErrorOr[Rate]] =
    for {
      maybeRate <- getCachedRate(requestPair.asString)
      errorOrRate <- getOrRefreshRates(requestPair, maybeRate)
    } yield errorOrRate

  private def getOrRefreshRates(requestPair: Rate.Pair, maybeRate: Option[Rate]): F[ProgramErrorOr[Rate]] =
    maybeRate.fold(
      refreshRates.map(findMatchingRateOrError(requestPair))
    )(rate => F.pure(rate.asRight[Error]))

  private def refreshRates: F[Map[String, Rate]] =
    for {
      getRatesOrError <- getFreshRates
      rates <- F.fromEither(getRatesOrError.leftMap(toProgramError))
      ratesMap = rates.toRatesMap
      _ <- Logger[F].info("Updating cache with fresh rates")
      _ <- setCachedRates(ratesMap)
    } yield ratesMap

  private def findMatchingRateOrError(requestPair: Rate.Pair)(ratesMap: Map[String, Rate]): ProgramErrorOr[Rate] =
    ratesMap
      .get(requestPair.asString)
      .toRight(CachedRateNotFound("Requested rate cannot be found"))

  private def getFlatRate(request: Protocol.GetRatesRequest): Rate =
    Rate(request.asPair, Price(1), Timestamp.now)
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
