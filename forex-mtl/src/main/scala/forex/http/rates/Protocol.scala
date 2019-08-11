package forex.http
package rates

import forex.domain._
import io.circe._
import io.circe.generic.semiauto._

object Protocol {

  final case class GetApiRequest(
      from: Currency,
      to: Currency
  )

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  final case class ApiErrorResponse(
      error: String
  )

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveEncoder[GetApiResponse]

  implicit val errorResponseEncoder: Encoder[ApiErrorResponse] =
    deriveEncoder[ApiErrorResponse]
}
