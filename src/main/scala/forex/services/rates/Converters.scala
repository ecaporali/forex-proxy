package forex.services.rates

import forex.domain.Rate

object Converters {

  import Protocol._

  private[rates] implicit class GetRatesResponseOps(val rateResponse: GetRatesResponse) {
    def toRates: List[Rate] =
      rateResponse.rates.map(
        rateResponse =>
          Rate(
            rateResponse.pair,
            price = rateResponse.price,
            timestamp = rateResponse.timestamp
        )
      )
  }
}
