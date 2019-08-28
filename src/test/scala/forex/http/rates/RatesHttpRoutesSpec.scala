package forex.http.rates

import cats.effect.IO
import forex.TestInstances.noopLogger
import forex.TestUtilsIO
import forex.domain.{Currency, RateFixtures}
import forex.http.rates.ProtocolFixtures.buildGetApiResponse
import forex.programs.rates.errors.Error.{CachedRateNotFound, RateLookupFailed, ServiceTemporaryUnavailable}
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.{Http4sLiteralSyntax, Method, Request, Response, Status, Uri}
import org.scalatest.{FreeSpec, Matchers}

class RatesHttpRoutesSpec extends FreeSpec with Matchers with Http4sDsl[IO] with TestUtilsIO {

  "RatesHttpRoutes" - {

    "getRates - success" - {
      val ratesHttpRoutesSuccess = new RatesHttpRoutes[IO](
        handleGetRate = _ => IO(Right(RateFixtures.buildRate()))
      )

      lazy val ratesEndpoints: List[(Request[IO], String, Status)] = List(
        (request(uri = uri"/v1/rates?from=AUD&to=JPY", method = GET), buildGetApiResponse().asJson.noSpaces, Status.Ok),
        (request(uri = uri"/v1/rates?from=WRONG_FROM&to=JPY", method = GET), """{"error":"Unknown currency code WRONG_FROM"}""", Status.BadRequest),
        (request(uri = uri"/v1/rates?from=AUD&to=WRONG_TO", method = GET), """{"error":"Unknown currency code WRONG_TO"}""", Status.BadRequest)
      )

      ratesEndpoints.foreach { endpoint =>
        val (req, expectedResponse, httpStatus) = endpoint

        s"should return the correct response for endpoint: ${req.method} ${req.uri}" in {
          val response: Response[IO] = runIO(ratesHttpRoutesSuccess.routes.run(req).value).get
          response.status shouldBe httpStatus
          bodyToString(response) shouldBe expectedResponse
        }
      }
    }

    "getRates - failures" - {
      val ratesHttpRoutesFailure = new RatesHttpRoutes[IO](
        handleGetRate = request =>
          request.from match {
            case Currency.AUD => IO(Left(RateLookupFailed("OneForge bad message!")))
            case Currency.EUR => IO(Left(CachedRateNotFound("The requested rate was not found!")))
            case Currency.JPY => IO(Left(ServiceTemporaryUnavailable("Too many requests sent!")))
            case _            => IO.raiseError(new RuntimeException("Uncaught BAD exception!"))
          }
      )

      lazy val ratesEndpoints: List[(Request[IO], String, Status)] = List(
        (request(uri = uri"/v1/rates?from=AUD&to=JPY", method = GET), """{"error":"OneForge bad message!"}""", Status.BadGateway),
        (request(uri = uri"/v1/rates?from=EUR&to=JPY", method = GET), """{"error":"The requested rate was not found!"}""", Status.NotFound),
        (request(uri = uri"/v1/rates?from=JPY&to=EUR", method = GET), """{"error":"Too many requests sent!"}""", Status.ServiceUnavailable),
        (request(uri = uri"/v1/rates?from=USD&to=JPY", method = GET), """{"error":"Uncaught BAD exception!"}""", Status.InternalServerError)
      )

      ratesEndpoints.foreach { endpoint =>
        val (req, expectedResponse, httpStatus) = endpoint

        s"should return failing response for endpoint: ${req.method} ${req.uri}" in {
          val response: Response[IO] = runIO(ratesHttpRoutesFailure.routes.run(req).value).get
          response.status shouldBe httpStatus
          bodyToString(response) shouldBe expectedResponse
        }
      }
    }
  }

  private def request(uri: Uri, method: Method): Request[IO] =
    Request(method = method, uri = uri)
}
