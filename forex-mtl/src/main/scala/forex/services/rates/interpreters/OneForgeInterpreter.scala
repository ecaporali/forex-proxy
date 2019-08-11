package forex.services.rates.interpreters

import cats.effect.Sync
import cats.implicits._
import forex.config.OneForgeConfig
import forex.domain.{ Price, Rate, Timestamp }
import forex.infrastructure.ErrorOr
import forex.services.rates.Algebra
import forex.services.rates.errors._
import org.http4s.Request

class OneForgeInterpreter[F[_]: Sync](
    config: OneForgeConfig,
    fetchRates: Request[F] => F[ErrorOr[Rate]]
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]

}
