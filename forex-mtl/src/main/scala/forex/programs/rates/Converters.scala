package forex.programs.rates

import forex.domain.Rate

object Converters {

  import Protocol._

  private[rates] implicit class GetRatesRequestOps(val getRatesRequest: GetRatesRequest) {
    def asPair: Rate.Pair =
      Rate.Pair(
        from = getRatesRequest.from,
        to = getRatesRequest.to
      )
  }

  private[rates] implicit class RateResponseOps(val rates: List[Rate]) {
    def asRatesMap: Map[Rate.Pair, Rate] =
      rates.map { rate =>
        (rate.pair, Rate(rate.pair, rate.price, rate.timestamp))
      }.toMap
  }
}
