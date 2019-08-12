package forex.services.rates

import forex.domain.Rate
import forex.services.ServiceErrorOr

trait Algebra[F[_]] {
  def getRates: F[ServiceErrorOr[Seq[Rate]]]
}
