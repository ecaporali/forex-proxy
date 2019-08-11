package forex.services.rates

import forex.services.ServiceErrorOr
import forex.services.rates.Protocol.GetRatesResponse

trait Algebra[F[_]] {
  def getRates: F[ServiceErrorOr[GetRatesResponse]]
}
