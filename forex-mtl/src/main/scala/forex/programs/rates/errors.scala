package forex.programs.rates

import forex.services.rates.errors.{ Error => RatesServiceError }

object errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateLookupFailed(msg: String) extends Error
    final case class UnknownCurrency(msg: String) extends Error
    final case class CachedRateNotFound(msg: String) extends Error
    final case class ServiceTemporaryUnavailable(msg: String) extends Error
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneForgeLookupFailed(msg)       => Error.RateLookupFailed(msg)
    case RatesServiceError.OneForgeQuotaLimitExceeded(msg) => Error.ServiceTemporaryUnavailable(msg)
  }
}
