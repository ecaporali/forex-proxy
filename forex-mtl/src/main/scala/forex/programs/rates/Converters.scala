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

  private[rates] implicit class RateResponseOps(val rates: Seq[Rate]) {
    def toRatesMap: Map[String, Rate] =
      rates.map { rate =>
        (rate.pair.asString, Rate(rate.pair, rate.price, rate.timestamp))
      }.toMap
  }
}
