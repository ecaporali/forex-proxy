package forex.programs.rates

import cats.effect.Clock
import cats.syntax.either._
import cats.syntax.eq._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.{FlatMap, MonadError}
import forex.config.RatesConfig
import forex.domain._
import forex.infrastructure.{CacheClient, Done}
import forex.programs.ProgramErrorOr
import forex.programs.rates.Converters._
import forex.programs.rates.errors.Error.{CachedRateNotFound, RateLookupFailed}
import forex.programs.rates.errors.{Error, toProgramError}
import forex.services.{RatesService, ServiceErrorOr}
import io.chrisdavenport.log4cats.Logger

class Program[F[_]: FlatMap: Logger: Clock] private[rates] (
    getFreshRates: => F[ServiceErrorOr[List[Rate]]],
    getCachedRate: Rate.Pair => F[Option[Rate]],
    setCachedRates: Map[Rate.Pair, Rate] => F[Done]
)(implicit F: MonadError[F, Throwable])
    extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[ProgramErrorOr[Rate]] =
    if (request.from === request.to) getFlatRate(request)
    else executeGetRequest(request.asPair)

  private def executeGetRequest(requestPair: Rate.Pair): F[ProgramErrorOr[Rate]] =
    for {
      maybeRate   <- getCachedRate(requestPair)
      errorOrRate <- maybeLookupRate(requestPair, maybeRate, getOrRefreshRates(requestPair, maybeRate))
    } yield errorOrRate

  // This method is synchronized to ensure requests are NOT made concurrently to refresh rates
  // The additional call to `getCachedRate` is required to verify that the cache was NOT updated while waiting for the lock to be released
  // If the synchronized keyword is removed, we might end up in the situation where requests could refresh rates concurrently hence exceeding the quota limit
  private def getOrRefreshRates(requestPair: Rate.Pair, maybeRate: Option[Rate]): F[ProgramErrorOr[Rate]] = synchronized {
    for {
      maybeConsistentRate <- getCachedRate(requestPair)
      errorOrRate         <- maybeLookupRate(requestPair, maybeConsistentRate, refreshRates.map(findMatchingRateOrError(requestPair)))
    } yield errorOrRate
  }

  private def maybeLookupRate(
      requestPair: Rate.Pair,
      maybeRate: Option[Rate],
      getRefreshedRate: => F[ProgramErrorOr[Rate]]
  ): F[ProgramErrorOr[Rate]] =
    maybeRate.map(rate => F.pure(rate.asRight[Error])).getOrElse(getRefreshedRate)

  private def refreshRates: F[Map[Rate.Pair, Rate]] =
    for {
      getRatesOrError <- getFreshRates
      rates           <- F.fromEither(getRatesOrError.leftMap(toProgramError))
      ratesMap = rates.asRatesMap
      _ <- Logger[F].info("Updating cache with fresh rates")
      _ <- setCachedRates(ratesMap)
    } yield ratesMap

  private def findMatchingRateOrError(requestPair: Rate.Pair)(ratesMap: Map[Rate.Pair, Rate]): ProgramErrorOr[Rate] =
    ratesMap
      .get(requestPair)
      .toRight(CachedRateNotFound("Requested rate cannot be found"))

  private def getFlatRate(request: Protocol.GetRatesRequest): F[ProgramErrorOr[Rate]] =
    Timestamp.now
      .map(Rate(request.asPair, Price(1), _).asRight[Error])
      .recover { case _: Throwable => RateLookupFailed("Cannot create instance of Timestamp, please try again!").asLeft[Rate] }
}

object Program {

  def apply[F[_]: Logger: Clock](
      config: RatesConfig,
      ratesService: RatesService[F],
      cacheClient: CacheClient[F]
  )(implicit F: MonadError[F, Throwable]): Algebra[F] = new Program[F](
    getFreshRates = ratesService.getRates,
    getCachedRate = cacheClient.getEntryValue[Rate.Pair, Rate](
      cacheKeyName = config.cacheKeyName
    ),
    setCachedRates = cacheClient.putEntries[Rate.Pair, Rate](
      cacheKeyName = config.cacheKeyName,
      timeout = Some(config.priceTimeout)
    )
  )
}
