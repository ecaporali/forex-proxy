package forex.services.rates

import forex.domain.Rate
import forex.services.ServiceErrorOr
import forex.services.rates.Protocol.GetRatesResponse

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[ServiceErrorOr[Rate]]
  def getRates: F[ServiceErrorOr[GetRatesResponse]]
}
