package forex.programs.rates

import forex.domain.Rate
import forex.services.rates.Protocol.RateResponse

object Converters {

  import Protocol._

  private[rates] implicit class GetRatesRequestOps(val getRatesRequest: GetRatesRequest) {
    def asPair: Rate.Pair =
      Rate.Pair(
        from = getRatesRequest.from,
        to = getRatesRequest.to
      )
  }

  private[rates] implicit class RateResponseOps(val rateResponse: RateResponse) {
    def toRateEntry: (String, Rate) =
      (
        rateResponse.pair.asString,
        Rate(
          rateResponse.pair,
          price = rateResponse.price,
          timestamp = rateResponse.timestamp
        )
      )
  }
}
