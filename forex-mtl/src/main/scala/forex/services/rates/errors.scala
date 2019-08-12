package forex.services.rates

object errors {

  sealed trait Error
  object Error {
    final case class OneForgeLookupFailed(msg: String) extends Error
    final case class OneForgeQuotaLimitExceeded(msg: String) extends Error
  }

}
