package forex

import forex.services.rates.Algebra
import forex.services.rates.errors.Error

package object services {
  type ServiceErrorOr[A]  = Error Either A
  type RatesService[F[_]] = Algebra[F]
  final val RatesServices = rates.Interpreters
}
